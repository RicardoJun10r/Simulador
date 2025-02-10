package com.trabalho.broker;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class BrokerQueue {

    private final String[] topics;
    private final String brokerUrl;
    private final int qos;
    private MqttClient mqttClient;
    private IBrokerQueue listenMethod;

    /**
     * Construtor.
     *
     * @param brokerUrl Endereço do broker.
     * @param topics    Array com os tópicos a serem utilizados.
     * @param qos       Qualidade do serviço.
     */
    public BrokerQueue(String brokerUrl, String[] topics, int qos) {
        this.brokerUrl = brokerUrl;
        this.topics = topics;
        this.qos = qos;
        initClient();
    }

    /**
     * Define o callback para mensagens recebidas.
     *
     * @param listenMethod Instância que implementa IBrokerQueue.
     */
    public void setListen(IBrokerQueue listenMethod) {
        this.listenMethod = listenMethod;
    }

    /**
     * Inicializa o cliente MQTT.
     */
    private void initClient() {
        try {
            String clientId = MqttClient.generateClientId();
            System.out.println("[*] Client ID: " + clientId);
            mqttClient = new MqttClient(brokerUrl, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            connectOptions.setAutomaticReconnect(true);
            connectOptions.setKeepAliveInterval(60);
            System.out.println("[*] Conectando ao broker " + brokerUrl);
            mqttClient.connect(connectOptions);
            System.out.println("[*] Conectado: " + mqttClient.isConnected());
        } catch (MqttException e) {
            System.err.println("Erro ao inicializar cliente MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia a inscrição nos tópicos e configura o callback para receber mensagens.
     */
    private void startListening() {
        if (listenMethod == null) {
            System.err.println("Callback não definido. Use setListen() para definir um método de escuta.");
            return;
        }
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("Conexão perdida: " + cause.getMessage());
                    cause.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    listenMethod.listen(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Método opcional para tratar a confirmação de entrega.
                }
            });
            if (topics != null && topics.length > 0) {
                System.out.println("[*] Inscrevendo no tópico: " + topics[0]);
                mqttClient.subscribe(topics[0], qos);
                System.out.println("[*] Inscrito com sucesso!");
            } else {
                System.err.println("Nenhum tópico informado para inscrição.");
            }
        } catch (MqttException e) {
            throw new RuntimeException("Erro durante a inscrição no tópico", e);
        }
    }

    /**
     * Publica uma mensagem no tópico especificado.
     *
     * @param topicIndex  Índice do tópico no array.
     * @param messageText Conteúdo da mensagem.
     */
    public void sendMessage(int topicIndex, String messageText) {
        if (topicIndex < 0 || topicIndex >= topics.length) {
            System.err.println("Índice de tópico inválido: " + topicIndex);
            return;
        }
        try {
            MqttMessage message = new MqttMessage(messageText.getBytes());
            message.setQos(qos);
            System.out.println("[*] Publicando mensagem: " + messageText);
            mqttClient.publish(topics[topicIndex], message);
        } catch (MqttException e) {
            System.err.println("Erro ao publicar mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Encerra a conexão com o broker.
     */
    public void close() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                System.out.println("[*] Desconectado do broker.");
                mqttClient.close();
            }
        } catch (MqttException e) {
            System.err.println("Erro ao fechar conexão MQTT: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia a escuta (subscription) conforme a configuração do callback.
     */
    public void start() {
        startListening();
    }
}
