package com.trabalho.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Conexao {

    private SimpleIntegerProperty id;
    private SimpleStringProperty endereco;
    private SimpleIntegerProperty porta;

    public Conexao(int id, String endereco, int porta) {
        this.id = new SimpleIntegerProperty(id);
        this.endereco = new SimpleStringProperty(endereco);
        this.porta = new SimpleIntegerProperty(porta);
    }

    public int getId() {
        return id.get();
    }

    public int getPorta() {
        return porta.get();
    }

    public String getEndereco() {
        return endereco.get();
    }

    public void setPorta(int value) {
        this.porta.set(value);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty enderecoProperty() {
        return endereco;
    }

    public SimpleIntegerProperty portaProperty() {
        return porta;
    }
}
