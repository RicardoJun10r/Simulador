package com.trabalho.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.trabalho.shared.Mensagem;

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

    public Integer getPort() {
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

    public static void enviarComoJson(Mensagem msg) {
        // Cria uma instância de Gson (ou reutilize uma estática)
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();

        String json = gson.toJson(msg);

        // Envia o JSON através do socket
        try (Socket s = new Socket("127.0.0.1", 4000);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8),
                        true)) {
            // Imprime o JSON no socket
            out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
