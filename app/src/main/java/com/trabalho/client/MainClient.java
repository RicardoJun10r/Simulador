package com.trabalho.client;

import com.trabalho.salaAula.Sala;

public class MainClient {
    public static void main(String[] args) {
        Sala sala = new Sala(14);
        sala.preencher();
        new Microcontrolador("12", "tcp://mqtt.eclipseprojects.io:1883", sala, true)
        .start();
    }
}
