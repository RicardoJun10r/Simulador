package com.trabalho.broker;

import java.lang.FunctionalInterface;

import org.eclipse.paho.client.mqttv3.MqttMessage;

@FunctionalInterface
public interface IBrokerQueue {
    void listen(String topico, MqttMessage mensagem);
}