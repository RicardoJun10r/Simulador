package com.trabalho.client;

import com.trabalho.salaAula.Sala;

public class MainClient {
    final static String BROKER = "tcp://mqtt.eclipseprojects.io:1883";
    final static String[] TOPICOS = { "segundo", "servidor_2" };

    public static void main(String[] args) {
        Sala sala = new Sala(5);
        sala.preencher();
        new Microcontrolador("25", BROKER, sala, TOPICOS, true)
                .start();
    }
}
