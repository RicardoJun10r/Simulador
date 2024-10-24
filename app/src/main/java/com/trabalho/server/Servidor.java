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

public class Servidor {

    /**
     * topico[0] --> fila que recebe
     * topico[1] --> mandar para fila
     */
    private String[] topico;

    private final String BROKER;

    private final Boolean DEBUG;

    private static Boolean FLAG;

    private static Scanner scanner;

    private static MqttClient mqtt;

    public Servidor(String endereco_broker, Boolean debug) {
        scanner = new Scanner(System.in);
        this.topico = new String[2];
        this.BROKER = endereco_broker;
        this.topico[0] = "servidor";
        this.topico[1] = "microcontrolador";
        this.DEBUG = debug;
        FLAG = true;
        this.initClient();
    }
    
    private void initClient(){
        try {
            String idCliente = MqttClient.generateClientId();
            System.out.println("[*] ID do Cliente: " + idCliente);
            mqtt = new MqttClient(BROKER, idCliente);
            MqttConnectOptions opcoesDaConexao = new MqttConnectOptions();
            opcoesDaConexao.setCleanSession(true);
            opcoesDaConexao.setAutomaticReconnect(true);
            opcoesDaConexao.setKeepAliveInterval(15);
            System.out.println("[*] Conectando-se ao broker " + BROKER);
            mqtt.connect(opcoesDaConexao);
            System.out.println("[*] Conectado: " + mqtt.isConnected());
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    
    private void initQueue() {
        try {
            final CountDownLatch trava = new CountDownLatch(20);
            
            mqtt.setCallback((MqttCallback) new MqttCallback() {
                public void messageArrived(String topico, MqttMessage mensagem) throws Exception {
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    if (DEBUG) {
                        System.out.println("\nUma mensagem foi recebida!" +
                                "\n\tData/Hora:    " + time +
                                "\n\tTópico:   " + topico +
                                "\n\tMensagem: " + new String(mensagem.getPayload()) +
                                "\n\tQoS:     " + mensagem.getQos() + "\n");
                    }
                    // trava.countDown();
                }

                public void connectionLost(Throwable causa) {
                    System.out.println("[*] Conexão com o broker foi perdida!" + causa.getMessage());
                    trava.countDown();
                }

                public void deliveryComplete(IMqttDeliveryToken token) {

                }

            });

            System.out.println("[*] Inscrevendo cliente no tópico: " + topico[0]);
            mqtt.subscribe(topico[0], 0);
            System.out.println("[*] Incrito!");

            try {
                trava.await();
            } catch (InterruptedException e) {
                System.out.println("[*] Me acordaram enquanto eu esperava zzzzz");
            }

            mqtt.disconnect();
            System.out.println("[*] Finalizando...");

            System.exit(0);
            mqtt.close();

        } catch (MqttException me) {
            throw new RuntimeException(me);
        }
    }

    private void sendQueue() {

        System.out.println("[*] Inicializando um publisher...");

        try {

            while (FLAG) {
                String conteudo;
                System.out.println("Digite o ID:");
                conteudo = scanner.next() + ".";

                System.out.println("Digite a operação:");
                conteudo += scanner.next();

                MqttMessage mensagem = new MqttMessage(conteudo.getBytes());

                mensagem.setQos(0);

                System.out.println("[*] Publicando mensagem: " + conteudo);

                mqtt.publish(topico[1], mensagem);

                System.out.println("[*] Mensagem publicada.");
            }
            mqtt.disconnect();
            System.exit(0);
            mqtt.close();

        } catch (MqttException me) {

            System.out.println("razão " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
        }
    }

    public void start() {
        new Thread(() -> {
            this.sendQueue();
        }).start();
        new Thread(() -> {
            this.initQueue();
        }).start();
    }

}
