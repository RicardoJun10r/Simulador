package com.trabalho.client;

import java.sql.Timestamp;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.trabalho.broker.BrokerQueue;
import com.trabalho.salaAula.Sala;

public class Microcontrolador {

    private final String id;
    private final boolean debug;
    private final Sala sala;
    private final Executor executor;
    private final BrokerQueue broker;

    /**
     * Construtor do microcontrolador.
     * 
     * @param id            Identificador deste microcontrolador.
     * @param brokerAddress Endereço do broker.
     * @param sala          Instância da sala para controle dos aparelhos.
     * @param debug         Se true, ativa mensagens de depuração.
     */
    public Microcontrolador(String id, String brokerAddress, Sala sala, boolean debug) {
        this.id = id;
        this.debug = debug;
        this.sala = sala;
        this.executor = Executors.newSingleThreadExecutor();

        String[] topicos = { "microcontrolador", "servidor" };
        this.broker = new BrokerQueue(brokerAddress, topicos, 0);

        // Configura o método de escuta para mensagens do broker.
        this.broker.setListen((topic, message) -> {
            String time = new Timestamp(System.currentTimeMillis()).toString();
            String content = new String(message.getPayload());

            if (debug) {
                System.out.println("\nMensagem recebida!" +
                        "\n\tData/Hora: " + time +
                        "\n\tTópico: " + topic +
                        "\n\tMensagem: " + content +
                        "\n\tQoS: " + message.getQos() + "\n");
            }
            System.out.println("Processando mensagem...");

            String[] parts = content.split("\\.");
            if (parts.length < 3) {
                System.err.println("Formato da mensagem inválido: " + content);
                return;
            }

            if (isValid(parts[0])) {
                String port = parts[2];
                String processedContent = processCommand(content);
                String response = id + "." + processedContent + "." + port;

                executor.execute(() -> broker.sendMessage(1, response));
                System.out.println("[*] Mensagem publicada.");
            }
        });
    }

    /**
     * Verifica se o id recebido é válido para este microcontrolador.
     * 
     * @param receivedId Id recebido na mensagem.
     * @return true se for igual ao próprio id ou for "-1"; caso contrário, false.
     */
    private boolean isValid(String receivedId) {
        return receivedId.equals(this.id) || receivedId.equals("-1");
    }

    /**
     * Processa o comando recebido e invoca o método correspondente na sala.
     * 
     * @param content Comando recebido no formato: "id.opcao.outroDado".
     * @return A resposta obtida após processar o comando.
     */
    private String processCommand(String content) {
        String[] parts = content.split("\\.");
        int option;
        try {
            option = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return "Comando Inválido!";
        }

        switch (option) {
            case 0:
            case 4:
                return sala.desligarAparelhos();
            case 1:
            case 3:
                return sala.ligarAparelhos();
            case 2:
            case 5:
                return sala.mostrarAparelhos();
            default:
                return "Comando Inválido!";
        }
    }

    public void start() {
        broker.start();
    }
}
