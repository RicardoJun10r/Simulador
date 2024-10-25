package com.trabalho.server;

public class MainServer {
    public static void main(String[] args) {
        new Servidor("127.0.0.1", 5000, "tcp://mqtt.eclipseprojects.io:1883", true)
                .start();
    }
}
