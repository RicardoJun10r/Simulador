package com.trabalho.client;

import com.trabalho.salaAula.Sala;

public class MainClient {
    public static void main(String[] args) {
        Sala sala = new Sala(2);
        sala.preencher();
        new Microcontrolador("1", "tcp://mqtt.eclipseprojects.io:1882", sala, true).start();
    }
}
