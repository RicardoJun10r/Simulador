package com.trabalho.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Conexao {

    private SimpleIntegerProperty id;

    private SimpleStringProperty endereco;

    private SimpleIntegerProperty porta;

    public Conexao(SimpleIntegerProperty id, SimpleStringProperty endereco, SimpleIntegerProperty porta) {
        this.id = id;
        this.endereco = endereco;
    }

    // Constructor
    public Conexao(int id, String endereco, int porta) {
        this.id = new SimpleIntegerProperty(id);
        this.endereco = new SimpleStringProperty(endereco);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getEndereco() {
        return endereco.get();
    }

    // Setters
    public void setPorta(int value) {
        this.porta.set(value);
    }

    // Property Getters
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
