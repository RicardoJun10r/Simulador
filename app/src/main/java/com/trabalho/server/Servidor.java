package com.trabalho.server;

import java.sql.Timestamp;
import java.util.Scanner;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;

public class Servidor {

    private final Boolean DEBUG;

    private static Boolean FLAG;

    private static Scanner scanner;

    private BrokerQueue broker;

    private IBrokerQueue listen_method;

    public Servidor(String endereco_broker, Boolean debug) {
        scanner = new Scanner(System.in);
        String[] TOPICO = { "servidor", "microcontrolador" };
        this.DEBUG = debug;
        FLAG = true;
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);
        this.listen_method = (topico, mensagem) -> {
            String time = new Timestamp(System.currentTimeMillis()).toString();
            if (DEBUG) {
                System.out.println("\nUma mensagem foi recebida!" +
                        "\n\tData/Hora:    " + time +
                        "\n\tTópico:   " + topico +
                        "\n\tMensagem: " + new String(mensagem.getPayload()) +
                        "\n\tQoS:     " + mensagem.getQos() + "\n");
            }
        };
        this.broker.setListen(listen_method);
    }

    private void sendQueue() {

        System.out.println("[*] Inicializando um publisher...");

        while (FLAG) {
            String conteudo;
            System.out.println("Digite o ID:");
            conteudo = scanner.next() + ".";

            System.out.println("Digite a operação:");

            conteudo += scanner.next();

            this.broker.sendMessage(1, conteudo);

            System.out.println("[*] Mensagem publicada.");
        }
        this.broker.close();
    }

    public void start() {
        new Thread(() -> {
            this.sendQueue();
        }).start();
        new Thread(() -> {
            this.broker.start();
        }).start();
    }

}
