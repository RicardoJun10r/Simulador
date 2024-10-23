package com.trabalho.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.trabalho.api.Interface.IMySocket;
import com.trabalho.api.Interface.ISocketConnectionsFunction;
import com.trabalho.api.Interface.ISocketListenFunction;
import com.trabalho.api.Interface.ISocketWriteFunction;


public class SocketServerSide extends IMySocket {

    private ServerSocket server;

    private Map<Integer, SocketClientSide> conexoes;

    private BlockingQueue<SocketClientSide> fila_escuta;

    private Integer contador_interno;

    private ISocketListenFunction metodo_escutar;

    private ISocketWriteFunction metodo_enviar;

    private ISocketConnectionsFunction atualizar_conexoes;

    private ExecutorService executorService;

    private SocketType TIPO;

    private final int NUMERO_THREADS = 6;

    public SocketServerSide(String endereco, int porta, SocketType TIPO) {
        super(endereco, porta);
        this.executorService = Executors.newFixedThreadPool(NUMERO_THREADS);
        this.conexoes = Collections.synchronizedMap(new HashMap<>());
        this.fila_escuta = new LinkedBlockingQueue<>();
        this.contador_interno = 0;
        this.TIPO = TIPO;
        this.atualizar_conexoes = null;
    }

    public Map<Integer, SocketClientSide> getConexoes() {
        return this.conexoes;
    }

    public SocketClientSide filaClientes() {
        try {
            return this.fila_escuta.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            return null;
        }
    }

    public void configurarUpdateConnections(ISocketConnectionsFunction atualizar_conexoes) {
        this.atualizar_conexoes = atualizar_conexoes;
    }

    public void iniciar() {
        try {
            this.server = new ServerSocket(super.getPorta(), 0, InetAddress.getByName(super.getEndereco()));
            System.out.println("INICIADO SERVIDOR NO IP: [" + super.getEndereco() + ":" + super.getPorta() + "]...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configurarMetodoEscutar(ISocketListenFunction metodo_escutar) {
        this.metodo_escutar = metodo_escutar;
    }

    public void configurarMetodoEnviar(ISocketWriteFunction metodo_enviar) {
        this.metodo_enviar = metodo_enviar;
    }

    public void enviar() {
        if (this.metodo_enviar != null) {
            this.executorService.execute(
                    () -> {
                        this.metodo_enviar.enviar();
                    });
        } else {
            System.out.println("ERRO: MÉTODO DO TIPO [ISocketWriteFunction] NÃO CONFIGURADO!");
        }
    }

    public void escutar() {
        if (this.metodo_escutar != null) {
            System.out.println("ESCUTANDO...");
            while (true) {
                try {
                    SocketClientSide socketClientSide = new SocketClientSide(this.server.accept());
                    socketClientSide.configurarEntradaSaida(TIPO);
                    adicionarConexao(socketClientSide);
                    this.executorService.execute(() -> this.metodo_escutar.escutar());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("ERRO: MÉTODO DO TIPO [ISocketListenFunction] NÃO CONFIGURADO!");
        }
    }

    public void fechar() {
        try {
            this.executorService.shutdown();
            this.server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void adicionar(SocketClientSide nova_conexao) {
        nova_conexao.configurarEntradaSaida(TIPO);
        adicionarConexao(nova_conexao);
    }

    private void adicionarConexao(SocketClientSide nova_conexao) {
        if (this.fila_escuta.add(nova_conexao)) {
            this.conexoes.put(contador_interno, nova_conexao);
            contador_interno++;
            if (this.atualizar_conexoes != null) {
                this.atualizar_conexoes.updateConnections();
            }
        }
    }

    public void unicast(Integer id, Object obj) {
        this.conexoes.get(id).enviarObjeto(obj);
    }

    public void unicast(Integer id, String msg) {
        this.conexoes.get(id).enviarMensagem(msg);
    }

    public void unicast(String endereco, int porta, String msg) {
        System.out.println("tentar enviar");
        this.conexoes.values()
                .stream()
                .filter(
                        conexao -> conexao.getEndereco().equals(endereco) && conexao.getPorta() == porta)
                .findFirst()
                .get()
                .enviarMensagem(msg);
    }

    public void unicast(String endereco, int porta, Object obj) {
        System.out.println("tentar enviar");
        this.conexoes.values()
                .stream()
                .filter(
                        conexao -> conexao.getEndereco().equals(endereco) && conexao.getPorta() == porta)
                .findFirst()
                .ifPresentOrElse(
                        conexao -> conexao.enviarObjeto(obj),
                        () -> System.out.println("Cliente não encontrado para o endereço e porta especificados."));
    }

    public void multicast(Integer[] id, String msg) {
        for (int i = 0; i < id.length; i++)
            if (this.conexoes.get(id[i]) != null)
                this.conexoes.get(id[i]).enviarMensagem(msg);
    }

    public void broadcast(String msg) {
        this.conexoes.forEach(
                (id, conexao) -> conexao.enviarMensagem(msg));
    }

    public void broadcast(Object obj) {
        this.conexoes.forEach(
                (id, conexao) -> conexao.enviarObjeto(obj));
    }

    public Boolean verificarConexao(String endereco, int porta) {
        Optional<SocketClientSide> cliente = this.conexoes.values()
                .stream()
                .filter(
                        conexao -> conexao.getEndereco().equals(endereco) && conexao.getPorta() == porta)
                .findFirst();
        if (cliente.isPresent()) {
            return true;
        } else {
            return false;
        }
    }

    public void listarConexoes() {
        this.conexoes.forEach(
                (id, conexao) -> System.out
                        .println(id + " --> [" + conexao.getEndereco() + ":" + conexao.getPorta() + "]"));
    }

}
