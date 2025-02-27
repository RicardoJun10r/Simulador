package com.trabalho.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;
import com.trabalho.controller.SimuladorController;
import com.trabalho.shared.Comando;
import com.trabalho.shared.Mensagem;
import com.trabalho.shared.ServerReq;
import com.trabalho.shared.ServerRes;
import com.trabalho.util.Aparelho;
import com.trabalho.util.ClientSocket;
import com.trabalho.util.Conexao;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class Servidor {

    private final boolean debug;
    private final String host;
    private final int port;
    private BrokerQueue broker;
    private IBrokerQueue listenMethod;
    private ServerSocket serverSocket;
    private final Executor executor;
    private final Map<Integer, ClientSocket> usuarios = Collections.synchronizedMap(new HashMap<>());
    private static final int NUM_THREADS = 6;
    private final BlockingQueue<Comando> commandQueue = new LinkedBlockingQueue<>();
    private ObservableList<Aparelho> observableAparelhos;
    private ObservableList<Conexao> observableServidores;
    private final List<Integer> microcontroladoresIds = Collections.synchronizedList(new ArrayList<>());

    public Servidor(String host, int port, String brokerAddress, String[] topicos, boolean debug,
            SimuladorController app) {
        this.host = host;
        this.port = port;
        this.debug = debug;
        this.observableAparelhos = (app != null) ? app.getMicrocontroladoresTable() : null;
        this.observableServidores = (app != null) ? app.getServidoresTable() : null;

        this.broker = new BrokerQueue(brokerAddress, topicos, 0);
        listenMicrocontrolador();
        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    /**
     * Inicializa a fila de mensagens (broker) caso não tenha sido configurado.
     */
    public void initQueue(String brokerAddress) {
        String[] topicos = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(brokerAddress, topicos, 0);
        listenMicrocontrolador();
    }

    private Integer hash(int num) {
        return num % 101;
    }

    private void addUsuario(Integer id, ClientSocket socket) {
        this.usuarios.put(id, socket);
        if (observableServidores != null) {
            Platform.runLater(() -> observableServidores.add(
                    new Conexao(id, socket.getSocketAddress().toString(), socket.getPort())));
        }
    }

    /**
     * Inicia o socket do servidor e os _threads_ para escuta de conexões e
     * comandos.
     */
    private void startServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.port, 0, InetAddress.getByName(this.host));
            System.out.println("INICIANDO: [" + this.host + ":" + this.port + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendWeb(new ServerRes(this.host, this.port, "start", "INICIANDO SERVIDOR", "0", 0));

        executor.execute(this::serverLoop);
        executor.execute(this::processCommandQueue);
    }

    /**
     * Loop principal que aceita novas conexões de clientes.
     */
    private void serverLoop() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientSocket cs = new ClientSocket(clientSocket, debug);
                executor.execute(() -> listenClient(cs));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addComando(Comando novo) {
        commandQueue.add(novo);
    }

    private String getMetaDados() {
        if (microcontroladoresIds.isEmpty()) {
            return "";
        }
        return microcontroladoresIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }

    /**
     * Processa os comandos recebidos na fila de comandos.
     */
    private void processCommandQueue() {
        Comando comando;
        do {
            try {
                comando = commandQueue.take();
                switch (comando.getOpcao()) {
                    case 0:
                        System.out.println("[*] Mensagem publicada.0");
                        broker.sendMessage(1,
                                comando.getMicrocontroladorId() + "." +
                                        comando.getMicrocontroladorOpcao() + "." + this.port);
                        System.out.println("[*] Mensagem publicada.");

                        sendWeb(new ServerReq(this.host, this.port,
                                "server-microcontroller." + getMetaDados(),
                                "CONTROLANDO SALA",
                                comando.getMicrocontroladorOpcao(),
                                comando.getMicrocontroladorId()));
                        break;
                    case 1:
                        unicast(comando.getServerId(),
                                new ServerReq(this.host, this.port, "request", "SERVIDOR",
                                        comando.getServerOpcao(),
                                        comando.getMicrocontroladorId()));
                        ClientSocket cs = this.usuarios.get(comando.getServerId());
                        if (cs != null) {
                            sendWeb(new ServerRes(
                                    host, port, "server-server." + getMetaDados(),
                                    "CONTROLANDO SERVIDOR." + getMetaDados() + "." + comando.getServerOpcao(),
                                    cs.getSocketAddress().toString(), cs.getPort()));
                        }
                        break;
                    case 2:
                        usuarios.forEach(
                                (id, socket) -> System.out.println(id + ": " + socket.getSocketAddress().toString()));
                        break;
                    case 3:
                        try {
                            Socket newSocket = new Socket(comando.getEndereco(), comando.getPorta());
                            ClientSocket novoCliente = new ClientSocket(newSocket, debug);
                            addUsuario(hash(comando.getPorta()), novoCliente);
                            novoCliente.send(new ServerReq(this.host, this.port, "ping", "", 0, 0));
                            sendWeb(new ServerRes(
                                    host, port, "server-server." + getMetaDados(),
                                    "INICIANDO CONEXAO", comando.getEndereco(), comando.getPorta()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println("ENTRADA INVÁLIDA");
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        } while (comando.getOpcao() != -1);
    }

    /**
     * Envia uma mensagem para a "web" (ou outro sistema) via JSON.
     */
    private void sendWeb(Mensagem msg) {
        new Thread(() -> {
            System.out.println("WEB");
            if (msg instanceof ServerReq) {
                ClientSocket.enviarComoJson((ServerReq) msg);
            } else if (msg instanceof ServerRes) {
                ClientSocket.enviarComoJson((ServerRes) msg);
            }
        }).start();
    }

    private void unicast(Integer id, Mensagem msg) {
        System.out.println(id + ": " + msg);
        ClientSocket cs = this.usuarios.get(id);
        if (cs != null) {
            cs.send(msg);
        }
    }

    private void unicast(String host, int port, String msg, String header) {
        try {
            ClientSocket cs = new ClientSocket(new Socket(host, port), false);
            cs.send(new ServerRes(host, port, header, msg, this.host, this.port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenClient(ClientSocket clientSocket) {
        if (clientSocket != null) {
            Mensagem message;
            while ((message = (Mensagem) clientSocket.read()) != null) {
                if (debug) {
                    System.out.println("DEBUG [" + clientSocket.getSocketAddress() + ":" +
                            clientSocket.getPort() + "]: " + message);
                }
                String header = message.getHeaders();
                switch (header.toLowerCase()) {
                    case "ping":
                        try {
                            ClientSocket newClient = new ClientSocket(
                                    new Socket(message.getEndereco(), message.getPorta()), debug);
                            newClient.send(new ServerRes(message.getEndereco(), message.getPorta(), "pong",
                                    "Servidor Alcancado!", this.host, this.port));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "pong":
                        if (message instanceof ServerRes) {
                            System.out.println(((ServerRes) message).getMensagem());
                        }
                        break;
                    case "request":
                        if (message instanceof ServerReq) {
                            ServerReq req = (ServerReq) message;
                            String reqMsg = req.getMicrocontroladorId() + "." + req.getOpcao() + "." + req.getPorta();
                            broker.sendMessage(1, reqMsg);
                        }
                        break;
                    case "response":
                        if (message instanceof ServerRes) {
                            ServerRes response = (ServerRes) message;
                            System.out.println(response.getMensagem());
                            atualizarTabela(response);
                        }
                        break;
                    default:
                        System.out.println("Header não reconhecido: " + header);
                        break;
                }
            }
        }
    }

    private void listenMicrocontrolador() {
        this.listenMethod = (topic, message) -> {
            String response = new String(message.getPayload());
            if (debug) {
                System.out.println("\nMensagem recebida!" +
                        "\n\tData/Hora: " + new Timestamp(System.currentTimeMillis()).toString() +
                        "\n\tTópico: " + topic +
                        "\n\tMensagem: " + response +
                        "\n\tQoS: " + message.getQos() + "\n");
            }
            String[] parts = response.split("\\.", 3);
            if (parts.length >= 3) {
                int id;
                try {
                    id = Integer.parseInt(parts[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Formato de id inválido: " + parts[0]);
                    return;
                }
                if (!microcontroladoresIds.contains(id)) {
                    microcontroladoresIds.add(id);
                }
                String messageContent = parts[1];
                String portStr = parts[2];

                if (messageContent.startsWith("Sala com")) {
                    String[] lines = messageContent.split("\\*");
                    int ligados = Integer.parseInt(lines[1].replaceAll("[^0-9]", ""));
                    int desligados = Integer.parseInt(lines[2].replaceAll("[^0-9]", ""));

                    Platform.runLater(() -> {
                        Aparelho aparelho = null;
                        for (Aparelho a : observableAparelhos) {
                            if (a.getId() == id) {
                                aparelho = a;
                                break;
                            }
                        }
                        if (aparelho == null) {
                            observableAparelhos.add(new Aparelho(
                                    id,
                                    serverSocket.getLocalSocketAddress().toString(),
                                    ligados,
                                    desligados));
                            System.out.println("Aparelho adicionado!");
                        } else {
                            aparelho.setAparelhosLigados(ligados);
                            aparelho.setAparelhosDesligados(desligados);
                            System.out.println("Aparelho atualizado!");
                        }
                    });
                }
                if (!portStr.equals(String.valueOf(this.port))) {
                    unicast(this.host, Integer.parseInt(portStr), response, "response");
                }
            } else {
                System.err.println("Formato da mensagem inesperado: " + response);
            }
        };
        this.broker.setListen(listenMethod);
    }

    private void atualizarTabela(ServerRes res) {
        String[] parts = res.getMensagem().split("\\.", 3);
        int id = Integer.parseInt(parts[0]);
        String messageContent = parts[1];

        if (messageContent.startsWith("Sala com")) {
            String[] lines = messageContent.split("\\*");
            int ligados = Integer.parseInt(lines[1].replaceAll("[^0-9]", ""));
            int desligados = Integer.parseInt(lines[2].replaceAll("[^0-9]", ""));

            Platform.runLater(() -> {
                Aparelho aparelho = null;
                for (Aparelho a : observableAparelhos) {
                    if (a.getId() == id) {
                        aparelho = a;
                        break;
                    }
                }
                if (aparelho == null) {
                    observableAparelhos.add(new Aparelho(
                            id,
                            "/" + res.getEnderecoResponse() + ":" + res.getPortaResponse(),
                            ligados,
                            desligados));
                    System.out.println("Aparelho adicionado!");
                } else {
                    aparelho.setAparelhosLigados(ligados);
                    aparelho.setAparelhosDesligados(desligados);
                    System.out.println("Aparelho atualizado!");
                }
            });
        }
    }

    public void startServer() {
        startServerSocket();
    }

    public void startQueue() {
        broker.start();
    }

    public void start() {
        new Thread(broker::start).start();
        startServerSocket();
    }
}
