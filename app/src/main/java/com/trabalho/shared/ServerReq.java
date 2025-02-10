package com.trabalho.shared;

public class ServerReq extends Mensagem {

    private int opcao;
    private int microcontroladorId;

    public ServerReq(String endereco, int porta, String headers, String mensagem, int opcao, int microcontroladorId) {
        super(endereco, porta, headers, mensagem);
        this.opcao = opcao;
        this.microcontroladorId = microcontroladorId;
    }

    public ServerReq(String mensagem, int opcao, int microcontroladorId) {
        super(mensagem);
        this.opcao = opcao;
        this.microcontroladorId = microcontroladorId;
    }

    public int getOpcao() {
        return opcao;
    }

    public void setOpcao(int opcao) {
        this.opcao = opcao;
    }

    public int getMicrocontroladorId() {
        return microcontroladorId;
    }

    public void setMicrocontroladorId(int microcontroladorId) {
        this.microcontroladorId = microcontroladorId;
    }

    @Override
    public String toString() {
        return "ServerReq [opcao=" + opcao + ", microcontroladorId=" + microcontroladorId +
                ", endereco=" + getEndereco() + ", porta=" + getPorta() +
                ", horario=" + getHorario() + ", headers=" + getHeaders() +
                ", mensagem=" + getMensagem() + "]";
    }
}
