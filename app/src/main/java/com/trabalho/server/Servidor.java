package com.trabalho.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;
import com.trabalho.shared.ServerReq;
import com.trabalho.util.ClientSocket;
import com.trabalho.util.MenuInterface;

public class Servidor {

    private final Boolean DEBUG;

    private final String HOST;

    private final Integer PORTA;

    private static Scanner scanner;

    private BrokerQueue broker;

    private IBrokerQueue listen_method;

    private ServerSocket serverSocket;

    private Executor executor;

    private Map<Integer, ClientSocket> USUARIOS = Collections.synchronizedMap(new HashMap<>());

    public Servidor(String host, int porta, String endereco_broker, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        scanner = new Scanner(System.in);
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMethod();
        this.executor = Executors.newFixedThreadPool(6);
    }

    public Servidor(String host, int porta, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        scanner = new Scanner(System.in);
        this.DEBUG = debug;
        this.executor = Executors.newFixedThreadPool(6);
    }

    private Integer hash(Integer num) {
        return num % 101;
    }

    private void add(Integer id, ClientSocket socket) {
        this.USUARIOS.put(id, socket);
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

    private void painel() {
        Integer op, microcontrolador, server, id_server, id_microcontrolador, porta;
        String endereco;
        do {
            MenuInterface.mostrarOpcoes();
            op = scanner.nextInt();
            switch (op) {
                case 0: {
                    MenuInterface.microcontroladorOpcoes();
                    microcontrolador = scanner.nextInt();
                    System.out.println("ID DO MICROCONTROLADOR OU (-1):");
                    id_microcontrolador = scanner.nextInt();
                    this.broker.sendMessage(1, (id_microcontrolador + "." + microcontrolador + "." + this.PORTA));
                    System.out.println("[*] Mensagem publicada.");
                    break;
                }
                case 1: {
                    MenuInterface.controlarServer();
                    server = scanner.nextInt();
                    System.out.println("ID DO SERVER:");
                    id_server = scanner.nextInt();
                    System.out.println("ID DO MICROCONTROLADOR OU (-1):");
                    id_microcontrolador = scanner.nextInt();
                    unicast(id_server, new ServerReq(this.HOST, this.PORTA, "request", "SERVIDOR", server,
                            id_microcontrolador));
                    break;
                }
                case 2: {
                    this.USUARIOS.forEach((id, socket) -> {
                        System.out.println(id + ": " + socket.getSocketAddress().toString());
                    });
                    break;
                }
                case 3: {
                    System.out.println("DIGITE O ENDEREÇO:");
                    endereco = scanner.next();
                    System.out.println("DIGITE A PORTA:");
                    porta = scanner.nextInt();
                    try {
                        ClientSocket novo = new ClientSocket(new Socket(endereco, porta), DEBUG);
                        add(hash(porta), novo);
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

        } while (op != -1);
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

            if (!(response.split("\\.")[1].equals(String.valueOf(this.PORTA)))) {
                this.USUARIOS.get(hash(Integer.parseInt(response.split("\\.")[1])))
                        .send(new ServerReq(this.HOST, this.PORTA, "response", response, -1, -1));
            }
        };
        this.broker.setListen(listen_method);
    }

    public void start() {
        new Thread(() -> {
            this.broker.start();
        }).start();
        this.server();
    }

    public void onlyserver() {
        this.server();
    }

}
