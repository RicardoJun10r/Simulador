package com.trabalho.broker;

import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BrokerQueue {

    private final String[] TOPICO;

    private final String BROKER;

    private static MqttClient mqtt;

    private IBrokerQueue listen_method;

    private Integer Qos;

    public BrokerQueue(String broker_uri, String[] topicos, int Qos) {
        this.BROKER = broker_uri;
        this.TOPICO = topicos;
        this.Qos = Qos;
        this.initClient();
    }

    public void setListen(IBrokerQueue listen_method) {
        this.listen_method = listen_method;
    }

    private void initClient() {
        try {
            String idCliente = MqttClient.generateClientId();
            System.out.println("[*] ID do Cliente: " + idCliente);
            mqtt = new MqttClient(BROKER, idCliente);
            MqttConnectOptions opcoesDaConexao = new MqttConnectOptions();
            opcoesDaConexao.setCleanSession(true);
            opcoesDaConexao.setAutomaticReconnect(true);
            opcoesDaConexao.setKeepAliveInterval(60);
            System.out.println("[*] Conectando-se ao broker " + BROKER);
            mqtt.connect(opcoesDaConexao);
            System.out.println("[*] Conectado: " + mqtt.isConnected());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void initQueue() {
        if (this.listen_method == null) {
            System.out.println("MÉTODO [IBrokerQueue listen_method] NÃO DEFINIDO");
            return;
        }
        try {

            final CountDownLatch trava = new CountDownLatch(20);

            mqtt.setCallback((MqttCallback) new MqttCallback() {
                public void messageArrived(String topico, MqttMessage mensagem) throws Exception {
                    listen_method.listen(topico, mensagem);
                    trava.countDown();
                }

                public void connectionLost(Throwable causa) {
                    System.out.println("Conexão com o broker foi perdida!" + causa.getMessage());
                    trava.countDown();
                }

                public void deliveryComplete(IMqttDeliveryToken token) {

                }

            });
            System.out.println("[*] Inscrevendo cliente no tópico: " + TOPICO[0]);
            mqtt.subscribe(TOPICO[0], this.Qos);
            System.out.println("[*] Incrito!");

            try {
                trava.await();
            } catch (InterruptedException e) {
                System.out.println("[*] Me acordaram enquanto eu esperava zzzzz");
            }
            this.close();
        } catch (MqttException me) {

            throw new RuntimeException(me);

        }
    }

    public void sendMessage(int topico, String mensage) {
        try {
            MqttMessage response = new MqttMessage(mensage.getBytes());
            response.setQos(this.Qos);
            System.out.println("[*] Publicando mensagem: " + mensage);
            mqtt.publish(TOPICO[topico], response);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            mqtt.disconnect();
            System.out.println("[*] Finalizando...");
            System.exit(0);
            mqtt.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        this.initQueue();
    }

}
