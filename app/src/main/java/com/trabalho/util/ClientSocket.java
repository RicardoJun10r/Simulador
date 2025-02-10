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

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final boolean debug;

    public ClientSocket(Socket socket, boolean debug) throws IOException {
        this.socket = socket;
        this.debug = debug;
        if (this.debug) {
            System.out.println("Cliente " + this.socket.getRemoteSocketAddress() + " conectado!");
        }
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
    }

    public SocketAddress getSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public void send(Object obj) {
        try {
            if (debug) {
                System.out.println("ENVIANDO: " + obj);
            }
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object read() {
        try {
            Object obj = in.readObject();
            if (debug) {
                System.out.println("LENDO: " + obj);
            }
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void closeRead() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWrite() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            closeRead();
            closeWrite();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia a mensagem como JSON para o servidor que escuta na porta 4000.
     * 
     * @param msg A mensagem a ser enviada.
     */
    public static void enviarComoJson(Mensagem msg) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
                .create();
        String json = gson.toJson(msg);
        try (Socket s = new Socket("127.0.0.1", 4000);
                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8),
                        true)) {
            out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
