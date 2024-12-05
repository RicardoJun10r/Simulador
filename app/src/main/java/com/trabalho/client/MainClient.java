package com.trabalho.client;

import com.trabalho.salaAula.Sala;

public class MainClient {
    final static String[] BROKER = {"tcp://mqtt.eclipseprojects.io:1883", "tcp://test.mosquitto.org:1883"};
        public static void main(String[] args) {
            Sala sala = new Sala(2);
            sala.preencher();
            new Microcontrolador("1", BROKER[0], sala, true)
        .start();
    }
}
