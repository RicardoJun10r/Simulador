package com.trabalho.client;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.trabalho.salaAula.Sala;

public class Microcontrolador implements Runnable {

    private final String ID;

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

    private final Sala SALA;

    private final BlockingQueue<String> comando;

    private static Boolean FLAG;

    public Microcontrolador(String id, String broker_servidor, Sala sala, Boolean debug) {
        this.comando = new LinkedBlockingQueue<>();
        this.broker = new String[2];
        this.topico = new String[2];
        this.broker[0] = "tcp://mqtt.eclipseprojects.io:1883";
        this.broker[1] = broker_servidor;
        this.topico[0] = "microcontrolador";
        this.topico[1] = "servidor";
        this.ID = id;
        this.DEBUG = debug;
        this.SALA = sala;
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
                    if(DEBUG){
                        System.out.println("\nUma mensagem foi recebida!" +
                                "\n\tData/Hora:    " + time +
                                "\n\tTópico:   " + topico +
                                "\n\tMensagem: " + new String(mensagem.getPayload()) +
                                "\n\tQoS:     " + mensagem.getQos() + "\n");
                    }
                    comando.add(new String(mensagem.getPayload()));
                    // trava.countDown();
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
                try {
                    conteudo = this.comando.take();
                    if(validar(conteudo.split("\\.")[0])){
                        conteudo = processarComando(conteudo);
                        
                        MqttMessage mensagem = new MqttMessage(conteudo.getBytes());
        
                        mensagem.setQos(0);
        
                        System.out.println("[*] Publicando mensagem: " + conteudo);
    
                        clienteMqtt.publish(topico[1], mensagem);
        
                        System.out.println("[*] Mensagem publicada.");
        
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
        this.initQueue();
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        this.sendQueue();
    }

}
