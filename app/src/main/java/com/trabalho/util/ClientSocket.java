package com.trabalho.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket {

    private final Socket SOCKET;

    private final ObjectOutputStream ESCREVER;

    private final ObjectInputStream LER;

    private final Boolean DEBUG;

    public ClientSocket(Socket socket, Boolean debug) throws IOException {
        this.SOCKET = socket;
        this.DEBUG = debug;
        if (this.DEBUG) {
            System.out.println("Cliente = " + this.SOCKET.getRemoteSocketAddress() + " conectado!");
        }
        this.ESCREVER = new ObjectOutputStream(this.SOCKET.getOutputStream());
        this.LER = new ObjectInputStream(this.SOCKET.getInputStream());
    }

    public SocketAddress getSocketAddress() {
        return this.SOCKET.getRemoteSocketAddress();
    }

    public Integer getPort(){
        return this.SOCKET.getPort();
    }

    public void send(Object obj) {
        try {
            if (this.DEBUG) {
                System.out.println("ENVIANDO: " + obj.toString());
            }
            this.ESCREVER.writeObject(obj);
            this.ESCREVER.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object read() {
        Object obj = null;
        try {
            obj = this.LER.readObject();
            if (this.DEBUG) {
                System.out.println("LENDO: " + obj.toString());
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void closeRead() {
        try {
            this.LER.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWrite() {
        try {
            this.ESCREVER.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.closeRead();
            this.closeWrite();
            this.SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
