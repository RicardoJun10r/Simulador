package com.trabalho.client;

import java.sql.Timestamp;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.broker.IBrokerQueue;
import com.trabalho.salaAula.Sala;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Microcontrolador {

    private final String ID;

    private final Boolean DEBUG;

    private final Sala SALA;

    private BrokerQueue broker;

    private IBrokerQueue listen_method;

    private Executor executor;

    public Microcontrolador(String id, String endereco_broker, Sala sala, Boolean debug) {
        String[]TOPICO = {"microcontrolador", "servidor"};
        this.ID = id;
        this.DEBUG = debug;
        this.SALA = sala;
        this.executor = Executors.newSingleThreadExecutor();
        this.broker = new BrokerQueue(endereco_broker, TOPICO, 0);

        this.listen_method = (topico, mensagem) -> {
            String time = new Timestamp(System.currentTimeMillis()).toString();
            String conteudo = new String(mensagem.getPayload());
            if(DEBUG){
                System.out.println("\nUma mensagem foi recebida!" +
                        "\n\tData/Hora:    " + time +
                        "\n\tTópico:   " + topico +
                        "\n\tMensagem: " + conteudo +
                        "\n\tQoS:     " + mensagem.getQos() + "\n");
            }
            System.out.println("Adicionando Mensagem...");

            if(validar(conteudo.split("\\.")[0])){
                conteudo = processarComando(conteudo);

                String response = conteudo;
 
                System.out.println("[*] Publicando mensagem: " + conteudo);

                executor.execute(() -> {
                    broker.sendMessage(1, response);
                });
 
                System.out.println("[*] Mensagem publicada.");

            }
        };
        this.broker.setListen(listen_method);
    }

    private Boolean validar(String id) {
        if (id.equals(this.ID) || id.equals("-1"))
            return true;
        else
            return false;
    }

    private String processarComando(String conteudo) {
        int op = Integer.parseInt(conteudo.split("\\.")[1]);
        switch (op) {
            case 0, 4: {
                return this.SALA.desligarAparelhos();
            }
            case 1, 3: {
                return this.SALA.ligarAparelhos();
            }
            case 2, 5: {
                return this.SALA.mostrarAparelhos();
            }
            default:
                return "Comando Inválido!";
        }
    }

    public void start() {
        broker.start();
    }

}
