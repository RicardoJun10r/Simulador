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
import com.trabalho.shared.Comando;
import com.trabalho.shared.ServerReq;
import com.trabalho.util.ClientSocket;
import com.trabalho.view.App;
import com.trabalho.view.App.Device;

import javafx.application.Platform;

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

    private App app;

    public Servidor(String host, int porta, String endereco_broker, Boolean debug, App app) {
        this.HOST = host;
        this.PORTA = porta;
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMethod();
        this.executor = Executors.newFixedThreadPool(N_THREADS);
        this.app = app;
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
            app.addConnection(new Device(id, socket.getSocketAddress().toString(), String.valueOf(socket.getPort())));
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
    }

    private void listen(ClientSocket cliente_socket) {
        if (cliente_socket != null) {
            ServerReq line;
            while ((line = (ServerReq) cliente_socket.read()) != null) {
                if (DEBUG) {
                    System.out.println("DEBUG [" + cliente_socket.getSocketAddress().toString() + ":"
                            + cliente_socket.getPort() + "]: " + line.toString());
                }
                String responseText = line.getMensagem();
                Platform.runLater(() -> {
                    String existingText = app.getResponses().getText();
                    app.getResponses().setText(existingText + "\n" + responseText);
                });

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
                    final String receivedLine = line.getMensagem();
                    Platform.runLater(() -> {
                        String existingText = app.getResponses().getText();
                        app.getResponses().setText(existingText + "\n" + receivedLine);
                    });
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

            Platform.runLater(() -> {
                String existingText = app.getResponses().getText();
                app.getResponses().setText(existingText + "\n" + response);
            });

            String[] parts = response.split("\\.");

            if (parts.length >= 2) {
                String idStr = parts[0];
                String messageContent = response.substring(idStr.length() + 1);

                int id = Integer.parseInt(idStr);

                app.addTopic(new App.Topic(id, messageContent));

                if (messageContent.startsWith("Sala com")) {
                    String[] lines = messageContent.split("\\*");
                    // String totalAparelhosLine = lines[0]; // "Sala com [ N ] aparelhos"
                    String ligadosLine = lines[1];
                    String desligadosLine = lines[2];

                    // int totalAparelhos = Integer.parseInt(totalAparelhosLine.replaceAll("[^0-9]", ""));
                    int ligados = Integer.parseInt(ligadosLine.replaceAll("[^0-9]", ""));
                    int desligados = Integer.parseInt(desligadosLine.replaceAll("[^0-9]", ""));

                    int statusId = id;
                    app.updateStatus(new App.Status(statusId, ligados, desligados, topico));
                }
            }

            if (!(parts[1].equals(String.valueOf(this.PORTA)))) {
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
