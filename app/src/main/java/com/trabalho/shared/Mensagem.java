package com.trabalho.shared;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Mensagem implements Serializable {

    private String endereco;
    private int porta;
    private LocalDateTime horario;
    private String headers;
    private String mensagem;

    public Mensagem(String endereco, int porta, String headers, String mensagem) {
        this.endereco = endereco;
        this.porta = porta;
        this.horario = LocalDateTime.now();
        this.headers = headers;
        this.mensagem = mensagem;
    }

    public Mensagem(String mensagem) {
        this.horario = LocalDateTime.now();
        this.mensagem = mensagem;
    }

    // Getters e setters
    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getPorta() {
        return porta;
    }

    public void setPorta(int porta) {
        this.porta = porta;
    }

    public LocalDateTime getHorario() {
        return horario;
    }

    public void setHorario(LocalDateTime horario) {
        this.horario = horario;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    @Override
    public String toString() {
        return "[ endereco = " + endereco + ", porta = " + porta + ", horario = " + horario +
                ", headers = " + headers + ", mensagem = " + mensagem + " ]";
    }
}
