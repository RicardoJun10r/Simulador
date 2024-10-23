package com.trabalho.server;

import java.sql.Timestamp;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Servidor implements Runnable {

    /**
     * topico[0] --> fila que recebe
     * topico[1] --> mandar para fila
     */
    private String[] topico;

    /**
     * broker[0] --> fila que recebe
     * broker[1] --> mandar para fila
     */
    private String[] broker;

    private final Boolean DEBUG;

    private static Boolean FLAG;

    private static Scanner scanner;

    public Servidor(String broker_servidor, Boolean debug) {
        scanner = new Scanner(System.in);
        this.broker = new String[2];
        this.topico = new String[2];
        this.broker[0] = broker_servidor;
        this.broker[1] = "tcp://mqtt.eclipseprojects.io:1883";
        this.topico[0] = "servidor";
        this.topico[1] = "microcontrolador";
        this.DEBUG = debug;
        FLAG = true;
    }

    private void initQueue() {
        try {
            String idCliente = MqttClient.generateClientId();
            System.out.println("[*] ID do Cliente: " + idCliente);
            MqttClient clienteMqtt = new MqttClient(broker[0], idCliente);

            MqttConnectOptions opcoesDaConexao = new MqttConnectOptions();
            opcoesDaConexao.setCleanSession(true);

            System.out.println("[*] Conectando-se ao broker " + broker);
            clienteMqtt.connect(opcoesDaConexao);
            System.out.println("[*] Conectado!");

            final CountDownLatch trava = new CountDownLatch(20);

            clienteMqtt.setCallback((MqttCallback) new MqttCallback() {
                public void messageArrived(String topico, MqttMessage mensagem) throws Exception {
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    if (DEBUG) {
                        System.out.println("\nUma mensagem foi recebida!" +
                                "\n\tData/Hora:    " + time +
                                "\n\tTópico:   " + topico +
                                "\n\tMensagem: " + new String(mensagem.getPayload()) +
                                "\n\tQoS:     " + mensagem.getQos() + "\n");
                    }
                    trava.countDown();
                }

                public void connectionLost(Throwable causa) {
                    System.out.println("Conexão com o broker foi perdida!" + causa.getMessage());
                    trava.countDown();
                }

                public void deliveryComplete(IMqttDeliveryToken token) {

                }

            });

            System.out.println("[*] Inscrevendo cliente no tópico: " + topico[0]);
            clienteMqtt.subscribe(topico[0], 0);
            System.out.println("[*] Incrito!");

            try {
                trava.await();
            } catch (InterruptedException e) {
                System.out.println("[*] Me acordaram enquanto eu esperava zzzzz");
            }

            clienteMqtt.disconnect();
            System.out.println("[*] Finalizando...");

            System.exit(0);
            clienteMqtt.close();
        } catch (MqttException me) {

            throw new RuntimeException(me);

        }
    }

    private void sendQueue() {

        System.out.println("[*] Inicializando um publisher...");

        try {

            String idCliente = MqttClient.generateClientId();
            System.out.println("[*] ID do Cliente: " + idCliente);
            MqttClient clienteMqtt = new MqttClient(broker[1], idCliente);
            MqttConnectOptions opcoesConexao = new MqttConnectOptions();
            opcoesConexao.setCleanSession(true);

            System.out.println("[*] Conectando-se ao broker " + broker);
            clienteMqtt.connect(opcoesConexao);
            System.out.println("[*] Conectado: " + clienteMqtt.isConnected());

            while (FLAG) {
                String conteudo;
                System.out.println("Digite o ID:");
                conteudo = scanner.next() + ".";

                System.out.println("Digite a operação:");
                conteudo += scanner.next();

                MqttMessage mensagem = new MqttMessage(conteudo.getBytes());

                mensagem.setQos(0);

                System.out.println("[*] Publicando mensagem: " + conteudo);

                clienteMqtt.publish(topico[1], mensagem);

                System.out.println("[*] Mensagem publicada.");
            }
            clienteMqtt.disconnect();
            System.exit(0);
            clienteMqtt.close();

        } catch (MqttException me) {

            System.out.println("razão " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
        }
    }

    public void start() {
        this.sendQueue();
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.initQueue();
    }

}
