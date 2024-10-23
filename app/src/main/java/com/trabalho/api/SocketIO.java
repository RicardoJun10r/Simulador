package com.trabalho.api;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.io.InputStreamReader;

public class SocketIO {

    private BufferedReader leitor_texto;

    private PrintWriter escritor_texto;

    private ObjectOutputStream escritor_objeto;

    private ObjectInputStream leitor_objeto;

    public SocketIO() {
    }

    public boolean enviar(String mensagem) {
        this.escritor_texto.println(mensagem);
        return !this.escritor_texto.checkError();
    }

    public String receber() {
        try {
            return this.leitor_texto.readLine();
        } catch (IOException e) {
            return "\nERRO: " + e.getMessage();
        }
    }

    public void enviarObjeto(Object object) {
        try {
            this.escritor_objeto.writeObject(object);
            this.escritor_objeto.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object receberObjeto() {
        Object object = null;
        try {
            object = this.leitor_objeto.readObject();
        } catch (EOFException e) {
            System.out.println("Conexão foi encerrada inesperadamente pelo cliente.");
            e.printStackTrace();
            this.fechar();
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Erro: Classe não encontrada durante a desserialização.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Erro de I/O ao receber objeto.");
            e.printStackTrace();
        }
        return object;
    }
    

    public void configurarTexto(Socket socket) {
        try {
            this.leitor_texto = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.escritor_texto = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configurarObjeto(Socket socket) {
        try {
            this.escritor_objeto = new ObjectOutputStream(socket.getOutputStream());
            this.escritor_objeto.flush();
            this.leitor_objeto = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedReader getLeitor_texto() {
        return leitor_texto;
    }

    public PrintWriter getEscritor_texto() {
        return escritor_texto;
    }

    public ObjectOutputStream getEscritor_objeto() {
        return escritor_objeto;
    }
    
    public ObjectInputStream getLeitor_objeto() {
        return leitor_objeto;
    }

    public void fechar() {
        try {
            if (this.escritor_objeto != null)
                this.escritor_objeto.close();
            if (this.escritor_texto != null)
                this.escritor_texto.close();
            if (this.leitor_objeto != null)
                this.leitor_objeto.close();
            if (this.leitor_texto != null)
                this.leitor_texto.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
