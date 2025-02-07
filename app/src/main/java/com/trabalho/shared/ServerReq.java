package com.trabalho.shared;

public class ServerReq extends Mensagem {

    private int opcao;

    private int microcontrolador_id;

    public ServerReq(String endereco, int porta, String headers, String mensagem, int opcao, int microcontrolador_id) {
        super(endereco, porta, headers, mensagem);
        this.opcao = opcao;
        this.microcontrolador_id = microcontrolador_id;
    }

    public ServerReq(String mensagem, int opcao, int microcontrolador_id) {
        super(mensagem);
        this.opcao = opcao;
        this.microcontrolador_id = microcontrolador_id;
    }

    public int getOpcao() {
        return opcao;
    }

    public void setOpcao(int opcao) {
        this.opcao = opcao;
    }

    public int getMicrocontrolador_id() {
        return microcontrolador_id;
    }

    public void setMicrocontrolador_id(int microcontrolador_id) {
        this.microcontrolador_id = microcontrolador_id;
    }

    @Override
    public String toString() {
        return "ServerReq [opcao=" + opcao + ", microcontrolador_id=" + microcontrolador_id + ", getEndereco()="
                + getEndereco() + ", getPorta()=" + getPorta() + ", getHorario()=" + getHorario() + ", getHeaders()="
                + getHeaders() + ", getMensagem()=" + getMensagem() + "]";
    }

}
