package com.trabalho.client;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.trabalho.salaAula.Sala;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Microcontrolador {

    private final String ID;

    /**
     * topico[0] --> fila que recebe
     * topico[1] --> mandar para fila
     */
    private final String[] TOPICO;

    private final String BROKER;

    private final Boolean DEBUG;

    private final Sala SALA;

    private static MqttClient mqtt;

    private Executor executor;

    public Microcontrolador(String id, String endereco_broker, Sala sala, Boolean debug) {
        this.TOPICO = new String[2];
        this.BROKER = endereco_broker;
        this.TOPICO[0] = "microcontrolador";
        this.TOPICO[1] = "servidor";
        this.ID = id;
        this.DEBUG = debug;
        this.SALA = sala;
        this.initClient();
        this.executor = Executors.newSingleThreadExecutor();
    }

    private void initClient(){
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
        try {

            final CountDownLatch trava = new CountDownLatch(20);

            mqtt.setCallback((MqttCallback) new MqttCallback() {
                public void messageArrived(String topico, MqttMessage mensagem) throws Exception {
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String conteudo = new String(mensagem.getPayload());
                    if(DEBUG){
                        System.out.println("\nUma mensagem foi recebida!" +
                                "\n\tData/Hora:    " + time +
                                "\n\tTópico:   " + topico +
                                "\n\tMensagem: " + conteudo +
                                "\n\tQoS:     " + mensagem.getQos() + "\n");
                    }
                    System.out.println("Adicionando Mensagem...");

                    if(validar(conteudo.split("\\.")[0])){
                        conteudo = processarComando(conteudo);
                        
                        MqttMessage response = new MqttMessage(conteudo.getBytes());
         
                        response.setQos(0);
         
                        System.out.println("[*] Publicando mensagem: " + conteudo);
                        
                        executor.execute(() -> {
                            try {
                                mqtt.publish(TOPICO[1], response);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                        });
         
                        System.out.println("[*] Mensagem publicada.");

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
            System.out.println("[*] Inscrevendo cliente no tópico: " + TOPICO[0]);
            mqtt.subscribe(TOPICO[0], 0);
            System.out.println("[*] Incrito!");

            try {
                trava.await();
            } catch (InterruptedException e) {
                System.out.println("[*] Me acordaram enquanto eu esperava zzzzz");
            }

            mqtt.disconnect();
            System.out.println("[*] Finalizando...");

            System.exit(0);
            // mqtt.close();
        } catch (MqttException me) {

            throw new RuntimeException(me);

        }
    }

    private Boolean validar(String id) {
        if (id.equals(this.ID))
            return true;
        else
            return false;
    }

    private String processarComando(String conteudo) {
        int op = Integer.parseInt(conteudo.split("\\.")[1]);
        switch (op) {
            case 0, 4: {
                return this.SALA.desligarAparelhos();
            }
            case 1, 3: {
                return this.SALA.ligarAparelhos();
            }
            case 2, 5: {
                return this.SALA.mostrarAparelhos();
            }
            default:
                return "Comando Inválido!";
        }
    }

    public void start() {
        this.initQueue();
    }

}
