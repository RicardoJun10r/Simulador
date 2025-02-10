package com.trabalho.shared;

public class Comando {

    private Integer microcontroladorId;
    private Integer serverId;
    private Integer microcontroladorOpcao;
    private Integer serverOpcao;
    private Integer opcao;
    private String endereco;
    private Integer porta;

    public Comando() {
    }

    public Comando(Integer opcao, String endereco, Integer porta) {
        this.opcao = opcao;
        this.endereco = endereco;
        this.porta = porta;
    }

    public Comando(Integer opcao, Integer microcontroladorId, Integer microcontroladorOpcao) {
        this.opcao = opcao;
        this.microcontroladorId = microcontroladorId;
        this.microcontroladorOpcao = microcontroladorOpcao;
    }

    public Comando(Integer opcao, Integer microcontroladorId, Integer serverId,
            Integer microcontroladorOpcao, Integer serverOpcao) {
        this.opcao = opcao;
        this.microcontroladorId = microcontroladorId;
        this.serverId = serverId;
        this.microcontroladorOpcao = microcontroladorOpcao;
        this.serverOpcao = serverOpcao;
    }

    public Comando(Integer opcao, Integer microcontroladorOpcao, Integer microcontroladorId,
            Integer serverOpcao, String endereco, Integer porta) {
        this.opcao = opcao;
        this.microcontroladorOpcao = microcontroladorOpcao;
        this.microcontroladorId = microcontroladorId;
        this.serverOpcao = serverOpcao;
        this.endereco = endereco;
        this.porta = porta;
    }

    // Getters e setters
    public Integer getOpcao() {
        return opcao;
    }

    public void setOpcao(Integer opcao) {
        this.opcao = opcao;
    }

    public Integer getMicrocontroladorId() {
        return microcontroladorId;
    }

    public void setMicrocontroladorId(Integer microcontroladorId) {
        this.microcontroladorId = microcontroladorId;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getMicrocontroladorOpcao() {
        return microcontroladorOpcao;
    }

    public void setMicrocontroladorOpcao(Integer microcontroladorOpcao) {
        this.microcontroladorOpcao = microcontroladorOpcao;
    }

    public Integer getServerOpcao() {
        return serverOpcao;
    }

    public void setServerOpcao(Integer serverOpcao) {
        this.serverOpcao = serverOpcao;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Integer getPorta() {
        return porta;
    }

    public void setPorta(Integer porta) {
        this.porta = porta;
    }
}
