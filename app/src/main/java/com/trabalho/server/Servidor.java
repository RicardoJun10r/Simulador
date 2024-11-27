package com.trabalho.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;
import com.trabalho.controller.SimuladorController;
import com.trabalho.shared.Comando;
import com.trabalho.shared.ServerReq;
import com.trabalho.util.Aparelho;
import com.trabalho.util.ClientSocket;
import com.trabalho.util.Conexao;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class Servidor {

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

    public Servidor(String host, int porta, String endereco_broker, Boolean debug, SimuladorController app) {
        this.HOST = host;
        this.PORTA = porta;
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMethod();
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
        this.listenMethod();
        this.executor = Executors.newFixedThreadPool(N_THREADS);
    }

    public Servidor(String host, int porta, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        this.DEBUG = debug;
        this.executor = Executors.newFixedThreadPool(N_THREADS);
    }

    public void initQueue(String endereco_broker){
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.listenMethod();
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
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                add(hash(socket.getPort()), socket);
                this.executor.execute(() -> {
                    this.listen(socket);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addComando(Comando novo){
        this.comandos.add(novo);
    }

    private void painel() {
        Comando novo_comando = new Comando();
        do {
            try {
                novo_comando = this.comandos.take();
                switch (novo_comando.getOpcao()) {
                    case 0: {
                        this.broker.sendMessage(1, (novo_comando.getMicrocontrolador_id() + "." + novo_comando.getMicrocontrolador_opcao() + "." + this.PORTA));
                        System.out.println("[*] Mensagem publicada.");
                        break;
                    }
                    case 1: {
                        unicast(novo_comando.getServer_id(), new ServerReq(this.HOST, this.PORTA, "request", "SERVIDOR", novo_comando.getServer_opcao(),
                                novo_comando.getMicrocontrolador_id()));
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
                            ClientSocket novo = new ClientSocket(new Socket(novo_comando.getEndereco(), novo_comando.getPorta()), DEBUG);
                            add(hash(novo_comando.getPorta()), novo);
                            novo.send(new ServerReq(this.HOST, this.PORTA, "handshake", "", 0, 0));
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

    private void unicast(Integer id, ServerReq msg) {
        this.USUARIOS.get(id).send(msg);
        Platform.runLater(() -> {

        });
    }

    private void listen(ClientSocket cliente_socket) {
        if (cliente_socket != null) {
            ServerReq line;
            while ((line = (ServerReq) cliente_socket.read()) != null) {
                if (DEBUG) {
                    System.out.println("DEBUG [" + cliente_socket.getSocketAddress().toString() + ":"
                            + cliente_socket.getPort() + "]: " + line.toString());
                }

                if (line.getHeaders().equalsIgnoreCase("handshake")) {
                    if (this.USUARIOS.containsKey(hash(line.getPorta()))) {
                        this.USUARIOS.get(hash(line.getPorta()))
                                .send(new ServerReq(this.HOST, this.PORTA, "response", "Servidor já adicionado", 0, 0));
                    } else {
                        ClientSocket novo;
                        try {
                            System.out.println("Adicionando");
                            novo = new ClientSocket(new Socket(line.getEndereco(), line.getPorta()), DEBUG);
                            add(hash(novo.getPort()), novo);
                            System.out.println("Enviando");
                            novo.send(new ServerReq(this.HOST, this.PORTA, "response", "Conexão realizada!", 0, 0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(line.getHeaders().equals("request")){
                    String req = line.getMicrocontrolador_id() + "." + line.getOpcao() + "." + line.getPorta();
                    this.broker.sendMessage(1, req);
                }
                if(line.getHeaders().equals("response")){
                    System.out.println(line.getMensagem());
                }
            }
        }
    }

    private void listenMethod() {
        this.listen_method = (topico, mensagem) -> {
            String response = new String(mensagem.getPayload());
    
            if (DEBUG) {
                System.out.println("\nUma mensagem foi recebida!" +
                        "\n\tData/Hora:    " + new Timestamp(System.currentTimeMillis()).toString() +
                        "\n\tTópico:   " + topico +
                        "\n\tMensagem: " + response +
                        "\n\tQoS:     " + mensagem.getQos() + "\n");
            }
    
            String[] parts = response.split("\\.");
    
            if (parts.length >= 2) {
                int id = Integer.parseInt(parts[0]);
                String messageContent = response.substring(parts[0].length() + 1);
    
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
                                    desligados
                            ));
                            System.out.println("Adicionado !");
                        } else {
                            aparelho.setAparelhosLigados(ligados);
                            aparelho.setAparelhosDesligados(desligados);
                            System.out.println("Atualizado !");
                        }
                    });
                }
            }
    
            if (!(parts[2].equals(String.valueOf(this.PORTA)))) {
                this.USUARIOS.get(hash(Integer.parseInt(parts[1])))
                        .send(new ServerReq(this.HOST, this.PORTA, "response", response, -1, -1));
            }
        };
        this.broker.setListen(listen_method);
    }

    public void startServer(){
        this.server();
    }

    public void startQueue(){
        this.broker.start();
    }

    public void start() {
        new Thread(() -> {
            this.broker.start();
        }).start();
        this.server();
    }

}
