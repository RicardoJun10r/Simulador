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

    private final String HOST_WEB = "localhost";

    private final Integer PORT_WEB = 4000;

    private final Boolean DEBUG;

    private final String HOST;

    private final Integer PORTA;

    private BrokerQueue broker;

    private IBrokerQueue listen_method;

    private ServerSocket serverSocket;

    private Executor executor;

    private Map<Integer, ClientSocket> USUARIOS = Collections.synchronizedMap(new HashMap<>());

    private final Integer N_THREADS = 6;

    private BlockingQueue<Comando> comandos = new LinkedBlockingQueue<>();

    private SimuladorController app;

    private ObservableList<Aparelho> observableListAparelhos;

    private ObservableList<Conexao> observableListServidores;

    private List<Integer> microcontroladores_ids = Collections.synchronizedList(new ArrayList<>());

    public Servidor(String host, int porta, String endereco_broker, Boolean debug, SimuladorController app) {
        this.HOST = host;
        this.PORTA = porta;
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMicrocontrolador();
        this.executor = Executors.newFixedThreadPool(N_THREADS);
        this.app = app;
        this.observableListAparelhos = this.app.getMicrocontroladoresTable();
        this.observableListServidores = this.app.getServidoresTable();
    }

    public Servidor(String host, int porta, String endereco_broker, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMicrocontrolador();
        this.executor = Executors.newFixedThreadPool(N_THREADS);
    }

    public Servidor(String host, int porta, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        this.DEBUG = debug;
        this.executor = Executors.newFixedThreadPool(N_THREADS);
    }

    public void initQueue(String endereco_broker) {
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.listenMicrocontrolador();
    }

    private Integer hash(Integer num) {
        return num % 101;
    }

    private void add(Integer id, ClientSocket socket) {
        this.USUARIOS.put(id, socket);
        Platform.runLater(() -> {
            this.observableListServidores.add(
                    new Conexao(id, socket.getSocketAddress().toString(), socket.getPort()));
        });
    }

    private void server() {

        try {
            this.serverSocket = new ServerSocket(this.PORTA, 0, InetAddress.getByName(this.HOST));
            System.out.println("INICIANDO: [" + this.HOST + ":" + this.PORTA + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.sendweb(new ServerRes(
            HOST, PORTA, "start", "INICIANDO SERVIDOR", "0", 0));

        this.executor.execute(() -> {
            serverLoop();
        });

        this.executor.execute(() -> {
            painel();
        });

    }

    private void serverLoop() {
        while (true) {
            try {
                ClientSocket socket = new ClientSocket(this.serverSocket.accept(), DEBUG);
                this.executor.execute(() -> {
                    this.listen(socket);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addComando(Comando novo) {
        this.comandos.add(novo);
    }

    private String metaDados(){
        if(this.microcontroladores_ids.isEmpty()){
            return "";
        }else {
            return this.microcontroladores_ids.stream().map(String::valueOf).collect(Collectors.joining("."));
        }
    }

    private void painel() {
        Comando novo_comando = new Comando();
        do {
            try {
                novo_comando = this.comandos.take();
                switch (novo_comando.getOpcao()) {
                    case 0: {
                        System.out.println("[*] Mensagem publicada.0");
                        this.broker.sendMessage(1, (novo_comando.getMicrocontrolador_id() + "."
                                + novo_comando.getMicrocontrolador_opcao() + "." + this.PORTA));
                        System.out.println("[*] Mensagem publicada.");

                        this.sendweb(new ServerReq(this.HOST, this.PORTA,
                                "server-microcontroller." + metaDados(),
                                "CONTROLANDO SALA",
                                novo_comando.getMicrocontrolador_opcao(),
                                novo_comando.getMicrocontrolador_id()));
                        break;
                    }
                    case 1: {
                        unicast(novo_comando.getServer_id(),
                                new ServerReq(this.HOST, this.PORTA, "request." + metaDados(), "SERVIDOR",
                                        novo_comando.getServer_opcao(),
                                        novo_comando.getMicrocontrolador_id()));
                        ClientSocket ex = this.USUARIOS.get(novo_comando.getServer_id());
                        this.sendweb(new ServerRes(
                            HOST, PORTA, "server-server", "req." + novo_comando.getServer_opcao(), ex.getSocketAddress().toString(), ex.getPort()));
                        break;
                    }
                    case 2: {
                        this.USUARIOS.forEach((id, socket) -> {
                            System.out.println(id + ": " + socket.getSocketAddress().toString());
                        });
                        break;
                    }
                    case 3: {
                        try {
                            ClientSocket novo = new ClientSocket(
                                    new Socket(novo_comando.getEndereco(), novo_comando.getPorta()), DEBUG);
                            add(hash(novo_comando.getPorta()), novo);
                            novo.send(new ServerReq(this.HOST, this.PORTA, "ping", "", 0, 0));
                            this.sendweb(new ServerRes(
                                HOST, PORTA, "server-server." + metaDados(), "INICIANDO CONEXAO", novo_comando.getEndereco(), novo_comando.getPorta()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    default: {
                        System.out.println("ENTRADA INVÁLIDA");
                        break;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (novo_comando.getOpcao() != -1);
    }

    private void sendweb(Mensagem msg) {
        new Thread(() -> {
            System.out.println("WEB");
            if (msg instanceof ServerReq) {
                ServerReq response = (ServerReq) msg;
                ClientSocket.enviarComoJson(response);
            }
            if (msg instanceof ServerRes) {
                ServerRes response = (ServerRes) msg;
                ClientSocket.enviarComoJson(response);
            }
        }).start();
    }

    private void unicast(Integer id, Mensagem msg) {
        System.out.println(id + ": " + msg);
        this.USUARIOS.get(id).send(msg);
    }

    private void unicast(String host, Integer port, String msg, String header) {
        ClientSocket clientSocket;
        try {
            clientSocket = new ClientSocket(new Socket(host, port), false);
            clientSocket.send(new ServerRes(host, port, header,
                    msg, this.HOST, this.PORTA));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listen(ClientSocket cliente_socket) {
        if (cliente_socket != null) {
            Mensagem line;
            while ((line = (Mensagem) cliente_socket.read()) != null) {
                if (DEBUG) {
                    System.out.println("DEBUG [" + cliente_socket.getSocketAddress().toString() + ":"
                            + cliente_socket.getPort() + "]: " + line.toString());
                }

                String headers = line.getHeaders();

                if (headers.equalsIgnoreCase("ping")) {
                    ClientSocket novo;
                    try {
                        novo = new ClientSocket(new Socket(line.getEndereco(), line.getPorta()), DEBUG);
                        novo.send(new ServerRes(line.getEndereco(), line.getPorta(), "pong",
                                "Servidor Alcancado!", this.HOST, this.PORTA));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (headers.equalsIgnoreCase("pong")) {
                    if (line instanceof ServerRes) {
                        ServerRes response = (ServerRes) line;
                        System.out.println(response.getMensagem());
                    }
                }
                if (headers.equals("request")) {
                    if (line instanceof ServerReq) {
                        ServerReq serverReq = (ServerReq) line;
                        String req = serverReq.getMicrocontrolador_id() + "." + serverReq.getOpcao() + "."
                                + serverReq.getPorta();
                        this.broker.sendMessage(1, req);
                    }
                }
                if (headers.equals("response")) {
                    if (line instanceof ServerRes) {
                        ServerRes response = (ServerRes) line;
                        System.out.println(response.getMensagem());
                        this.atualizarTabela(response);
                    }
                }
            }
        }
    }

    private void listenMicrocontrolador() {
        this.listen_method = (topico, mensagem) -> {
            String response = new String(mensagem.getPayload());

            if (DEBUG) {
                System.out.println("\nUma mensagem foi recebida!" +
                        "\n\tData/Hora:    " + new Timestamp(System.currentTimeMillis()).toString() +
                        "\n\tTópico:   " + topico +
                        "\n\tMensagem: " + response +
                        "\n\tQoS:     " + mensagem.getQos() + "\n");
            }

            String[] parts = response.split("\\.", 3);

            if (parts.length >= 3) {
                int id = Integer.parseInt(parts[0]);

                if(!this.microcontroladores_ids.contains(id)) 
                    this.microcontroladores_ids.add(id);
                
                String messageContent = parts[1];
                String porta = parts[2];

                if (messageContent.startsWith("Sala com")) {
                    String[] lines = messageContent.split("\\*");
                    int ligados = Integer.parseInt(lines[1].replaceAll("[^0-9]", ""));
                    int desligados = Integer.parseInt(lines[2].replaceAll("[^0-9]", ""));

                    Platform.runLater(() -> {
                        Aparelho aparelho = null;
                        for (Aparelho a : this.observableListAparelhos) {
                            if (a.getId() == id) {
                                aparelho = a;
                                break;
                            }
                        }

                        if (aparelho == null) {
                            this.observableListAparelhos.add(new Aparelho(
                                    id,
                                    this.serverSocket.getLocalSocketAddress().toString(),
                                    ligados,
                                    desligados));
                            System.out.println("Adicionado !");
                        } else {
                            aparelho.setAparelhosLigados(ligados);
                            aparelho.setAparelhosDesligados(desligados);
                            System.out.println("Atualizado !");
                        }
                    });
                }

                if (!(porta.equals(String.valueOf(this.PORTA)))) {
                    this.unicast(this.HOST, Integer.parseInt(porta), response, "response");
                }
            } else {
                System.err.println("Received message in unexpected format: " + response);
            }
        };

        this.broker.setListen(listen_method);
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
                for (Aparelho a : this.observableListAparelhos) {
                    if (a.getId() == id) {
                        aparelho = a;
                        break;
                    }
                }

                if (aparelho == null) {
                    this.observableListAparelhos.add(new Aparelho(
                            id,
                            "/" + res.getEndereco_response() + ":" + res.getPorta_response(),
                            ligados,
                            desligados));
                    System.out.println("Adicionado !");
                } else {
                    aparelho.setAparelhosLigados(ligados);
                    aparelho.setAparelhosDesligados(desligados);
                    System.out.println("Atualizado !");
                }
            });
        }
    }

    public void startServer() {
        this.server();
    }

    public void startQueue() {
        this.broker.start();
    }

    public void start() {
        new Thread(() -> {
            this.broker.start();
        }).start();
        this.server();
    }

}
