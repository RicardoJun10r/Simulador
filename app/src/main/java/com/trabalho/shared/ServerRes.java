package com.trabalho.shared;

public class ServerRes extends Mensagem {

    private String endereco_response;
    
    private Integer porta_response;
    
    public ServerRes(String endereco, int porta, String headers, String mensagem, String endereco_response,
            Integer porta_response) {
        super(endereco, porta, headers, mensagem);
        this.endereco_response = endereco_response;
        this.porta_response = porta_response;
    }

    public ServerRes(String mensagem, String endereco_response, Integer porta_response) {
        super(mensagem);
        this.endereco_response = endereco_response;
        this.porta_response = porta_response;
    }

    public String getEndereco_response() {
        return endereco_response;
    }

    public void setEndereco_response(String endereco_response) {
        this.endereco_response = endereco_response;
    }

    public Integer getPorta_response() {
        return porta_response;
    }

    public void setPorta_response(Integer porta_response) {
        this.porta_response = porta_response;
    }

    @Override
    public String toString() {
        return "ServerRes [endereco_response=" + endereco_response + ", porta_response=" + porta_response
                + ", getEndereco()=" + getEndereco() + ", getPorta()=" + getPorta() + ", getHorario()=" + getHorario()
                + ", getHeaders()=" + getHeaders() + ", getMensagem()=" + getMensagem() + "]";
    }
    
}
