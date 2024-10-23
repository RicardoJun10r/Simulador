package com.trabalho.api;

import java.net.Socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.trabalho.api.Interface.IMySocket;
import com.trabalho.api.Interface.ISocketListenFunction;
import com.trabalho.api.Interface.ISocketWriteFunction;

public class SocketClientSide extends IMySocket {

    private Socket socket;

    private final int TIME_OUT = 5 * 1000;

    private SocketIO entrada_saida;

    private ISocketListenFunction metodo_escutar;

    private ISocketWriteFunction metodo_enviar;

    private ExecutorService executorService;

    private final int NUMERO_THREADS = 6;

    public SocketClientSide(String endereco, int porta) {
        super(endereco, porta);
        this.entrada_saida = new SocketIO();
        this.executorService = Executors.newFixedThreadPool(NUMERO_THREADS);
        this.socket = new Socket();
    }

    public SocketClientSide(Socket socket){
        super(socket.getInetAddress().getHostAddress(), socket.getPort());
        this.socket = socket;
        this.entrada_saida = new SocketIO();
        this.executorService = Executors.newFixedThreadPool(NUMERO_THREADS);
    }

    public void conectar() {
        System.out.println("TENTANDO CONEXÃO NO IP: [" + super.getEndereco() + ":" + super.getPorta() + "]...");
        try {
            this.socket.connect(new InetSocketAddress(super.getEndereco(), super.getPorta()), TIME_OUT);
            System.out.println("CONEXÃO ESTABELECIDA!");
        } catch (IOException e) {
            System.out.println("ERRO NA CONEXÃO: " + e.getMessage());
        }
    }

    public Socket getSocket(){ return this.socket; }
    
    public void configurarMetodoEscutar(ISocketListenFunction metodo_escutar){ this.metodo_escutar = metodo_escutar; }

    public void configurarMetodoEnviar(ISocketWriteFunction metodo_enviar){ this.metodo_enviar = metodo_enviar; }

    public void configurarEntradaSaida(SocketType opcao) {
        switch (opcao) {
            case TEXTO: {
                this.entrada_saida.configurarTexto(this.socket);
                break;
            }
            case OBJETO: {
                this.entrada_saida.configurarObjeto(this.socket);
                break;
            }
            case TODOS: {
                this.entrada_saida.configurarTexto(this.socket);
                this.entrada_saida.configurarObjeto(this.socket);
                break;
            }
            default: {
                System.out.println("NENHUMA OPÇÃO DIGITADA, FECHANDO CONEXÃO!");
                try {
                    this.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void enviarMensagem(String msg){
        this.entrada_saida.enviar(msg);
    }

    public String receberMensagem(){
        return this.entrada_saida.receber();
    }

    public void enviarObjeto(Object obj){
        this.entrada_saida.enviarObjeto(obj);
    }

    public Object receberObjeto(){
        if(this.entrada_saida != null) return this.entrada_saida.receberObjeto();
        else return null;
    }

    public void enviar(){
        if(this.metodo_enviar != null){
            this.executorService.execute(
                () -> {
                    this.metodo_enviar.enviar();
                }
            );
        } else {
            System.out.println("ERRO: MÉTODO DO TIPO [ISocketWriteFunction] NÃO CONFIGURADO!");
        }
    }

    public void escutar(){
        if(this.metodo_escutar != null){
            this.executorService.execute(
                () -> {
                    this.metodo_escutar.escutar();
                }
            );
        } else {
            System.out.println("ERRO: MÉTODO DO TIPO [ISocketListenFunction] NÃO CONFIGURADO!");
        }
    }

    public void fechar(){
        try {
            this.executorService.shutdown();
            this.entrada_saida.fechar();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
