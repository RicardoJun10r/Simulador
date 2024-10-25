package com.trabalho.server;

import java.sql.Timestamp;
import java.util.Scanner;

import com.trabalho.api.SocketClientSide;
import com.trabalho.api.SocketServerSide;
import com.trabalho.api.SocketType;
import com.trabalho.api.Interface.ISocketListenFunction;
import com.trabalho.api.Interface.ISocketWriteFunction;
import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;
import com.trabalho.shared.ServerReq;
import com.trabalho.util.MenuInterface;

public class Servidor {

    private final Boolean DEBUG;

    private final String HOST;

    private final Integer PORTA;

    private static Scanner scanner;

    private BrokerQueue broker;

    private IBrokerQueue listen_method;

    private ISocketListenFunction metodo_escutar;

    private ISocketWriteFunction metodo_enviar;

    private SocketServerSide server;

    public Servidor(String host, int porta, String endereco_broker, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        scanner = new Scanner(System.in);
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.DEBUG = debug;
        this.listenMethod();
    }

    public Servidor(String host, int porta, Boolean debug) {
        this.HOST = host;
        this.PORTA = porta;
        scanner = new Scanner(System.in);
        this.DEBUG = debug;
    }

    private void server(){
        this.server = new SocketServerSide(this.HOST, this.PORTA, SocketType.OBJETO);

        this.server.iniciar();

        this.metodo_escutar = () -> {
            SocketClientSide cliente = this.server.filaClientes();
            if (cliente != null) {
                ServerReq line;
                while ((line = (ServerReq) cliente.receberObjeto()) != null) {
                    if(DEBUG){
                        System.out.println("DEBUG [" + cliente.getEndereco() + ":" + cliente.getPorta() + "]: " + line.toString());
                    }
                    if(line.getHeaders().equalsIgnoreCase("handshake")){
                        adicionarConexao(line.getEndereco(), line.getPorta());
                        this.server.unicast(line.getEndereco(), line.getPorta(), new ServerReq(this.HOST, line.getPorta(), "response", "", -1, -1));
                    }
                }
            }
        };

        this.metodo_enviar = () -> {
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
                        this.broker.sendMessage(1, (id_microcontrolador + "." + microcontrolador));
                        System.out.println("[*] Mensagem publicada."); 
                        break;
                    }
                    case 1: {
                        MenuInterface.controlarServer();
                        server = scanner.nextInt();
                        System.out.println("ID DO SERVER:");
                        id_server = scanner.nextInt();
                        if(server <= 2){
                            System.out.println("ID DO MICROCONTROLADOR:");
                            id_microcontrolador = scanner.nextInt();
                            this.server.unicast(id_server, new ServerReq(this.HOST, this.PORTA, "fwd", "SERVIDOR", server, id_microcontrolador));
                        } else {
                            this.server.unicast(id_server, new ServerReq(this.HOST, this.PORTA, "fwd", "SERVIDOR", server, -1));
                        }
                        break;
                    }
                    case 2: {
                        this.server.listarConexoes();
                        break;
                    }
                    case 3: {
                        System.out.println("DIGITE O ENDEREÇO:");
                        endereco = scanner.next();
                        System.out.println("DIGITE A PORTA:");
                        porta = scanner.nextInt();
                        adicionarConexao(endereco, porta);
                        break;
                    }
                    default: {
                        System.out.println("ENTRADA INVÁLIDA");
                        break;
                    }
                }

            } while (op != -1);
        };

        this.server.configurarMetodoEscutar(metodo_escutar);

        this.server.configurarMetodoEnviar(metodo_enviar);

        this.server.enviar();

        this.server.escutar();
    }

    private void adicionarConexao(String endereco, int porta){
        SocketClientSide nova_conexao = new SocketClientSide(endereco, porta);
        nova_conexao.conectar();
        this.server.adicionar(nova_conexao);
        nova_conexao.enviarObjeto(new ServerReq(endereco, porta, "handshake", "", -1, -1));
    }

    private void listenMethod(){
        this.listen_method = (topico, mensagem) -> {
            if (DEBUG) {
                System.out.println("\nUma mensagem foi recebida!" +
                        "\n\tData/Hora:    " + new Timestamp(System.currentTimeMillis()).toString() +
                        "\n\tTópico:   " + topico +
                        "\n\tMensagem: " + new String(mensagem.getPayload()) +
                        "\n\tQoS:     " + mensagem.getQos() + "\n");
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

    public void onlyserver(){
        this.server();
    }

}
