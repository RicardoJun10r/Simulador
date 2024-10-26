package com.trabalho.shared;

public class Comando {

    private Integer microcontrolador_id;

    private Integer server_id;

    private Integer microcontrolador_opcao;

    private Integer server_opcao;

    private Integer opcao;

    private String endereco;

    private Integer porta;

    public Comando() {}

    public Comando(String endereco, Integer porta) {
        this.endereco = endereco;
        this.porta = porta;
    }

    public Comando(Integer opcao, Integer microcontrolador_id, Integer microcontrolador_opcao) {
        this.opcao = opcao;
        this.microcontrolador_id = microcontrolador_id;
        this.microcontrolador_opcao = microcontrolador_opcao;
    }

    public Comando(Integer opcao, Integer microcontrolador_id, Integer server_id, Integer microcontrolador_opcao,
                   Integer server_opcao) {
        this.opcao = opcao;
        this.microcontrolador_id = microcontrolador_id;
        this.server_id = server_id;
        this.microcontrolador_opcao = microcontrolador_opcao;
        this.server_opcao = server_opcao;
    }

    // New Constructor
    public Comando(Integer opcao, Integer microcontrolador_opcao, Integer microcontrolador_id, Integer server_opcao,
                   String endereco, Integer porta) {
        this.opcao = opcao;
        this.microcontrolador_opcao = microcontrolador_opcao;
        this.microcontrolador_id = microcontrolador_id;
        this.server_opcao = server_opcao;
        this.endereco = endereco;
        this.porta = porta;
    }


    public Integer getOpcao() {
        return opcao;
    }

    public void setOpcao(Integer opcao) {
        this.opcao = opcao;
    }

    public Integer getMicrocontrolador_id() {
        return microcontrolador_id;
    }

    public void setMicrocontrolador_id(Integer microcontrolador_id) {
        this.microcontrolador_id = microcontrolador_id;
    }

    public Integer getServer_id() {
        return server_id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }

    public Integer getMicrocontrolador_opcao() {
        return microcontrolador_opcao;
    }

    public void setMicrocontrolador_opcao(Integer microcontrolador_opcao) {
        this.microcontrolador_opcao = microcontrolador_opcao;
    }

    public Integer getServer_opcao() {
        return server_opcao;
    }

    public void setServer_opcao(Integer server_opcao) {
        this.server_opcao = server_opcao;
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
