package com.trabalho.client;

import com.trabalho.salaAula.Sala;

public class MainClient {
    final static String[] BROKER = { "tcp://mqtt.eclipseprojects.io:1883", "tcp://test.mosquitto.org:1883",
            "tcp://broker.hivemq.com:1883" };
    final static String[] TOPICOS = { "sala1", "servidor1" };

    public static void main(String[] args) {
        Sala sala = new Sala(16);
        sala.preencher();
        new Microcontrolador("20", BROKER[0], sala, TOPICOS, true)
                .start();
    }
}
