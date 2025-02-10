package com.trabalho.shared;

public class ServerRes extends Mensagem {

    private String enderecoResponse;
    private Integer portaResponse;

    public ServerRes(String endereco, int porta, String headers, String mensagem, String enderecoResponse,
            Integer portaResponse) {
        super(endereco, porta, headers, mensagem);
        this.enderecoResponse = enderecoResponse;
        this.portaResponse = portaResponse;
    }

    public ServerRes(String mensagem, String enderecoResponse, Integer portaResponse) {
        super(mensagem);
        this.enderecoResponse = enderecoResponse;
        this.portaResponse = portaResponse;
    }

    public String getEnderecoResponse() {
        return enderecoResponse;
    }

    public void setEnderecoResponse(String enderecoResponse) {
        this.enderecoResponse = enderecoResponse;
    }

    public Integer getPortaResponse() {
        return portaResponse;
    }

    public void setPortaResponse(Integer portaResponse) {
        this.portaResponse = portaResponse;
    }

    @Override
    public String toString() {
        return "ServerRes [enderecoResponse=" + enderecoResponse + ", portaResponse=" + portaResponse +
                ", endereco=" + getEndereco() + ", porta=" + getPorta() + ", horario=" + getHorario() +
                ", headers=" + getHeaders() + ", mensagem=" + getMensagem() + "]";
    }
}
