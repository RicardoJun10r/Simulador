package com.trabalho.server;

public class MainServer2 {
    public static void main(String[] args) {
        new Servidor("127.0.0.1", 1026, true)
                .onlyserver();
    }
}
