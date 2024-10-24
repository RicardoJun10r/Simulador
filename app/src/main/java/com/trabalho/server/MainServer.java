package com.trabalho.server;

public class MainServer {
    public static void main(String[] args) {
        new Servidor("tcp://mqtt.eclipseprojects.io:1883", true)
        .start();
    }
}
