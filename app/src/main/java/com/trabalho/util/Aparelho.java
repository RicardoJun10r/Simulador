package com.trabalho.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Aparelho {

    private SimpleIntegerProperty id;

    private SimpleStringProperty endereco;

    private SimpleIntegerProperty aparelhosLigados;

    private SimpleIntegerProperty aparelhosDesligados;

    public Aparelho(SimpleIntegerProperty id, SimpleStringProperty endereco, SimpleIntegerProperty aparelhosLigados,
            SimpleIntegerProperty aparelhosDesligados) {
        this.id = id;
        this.endereco = endereco;
        this.aparelhosLigados = aparelhosLigados;
        this.aparelhosDesligados = aparelhosDesligados;
    }

    // Constructor
    public Aparelho(int id, String endereco, int aparelhosLigados, int aparelhosDesligados) {
        this.id = new SimpleIntegerProperty(id);
        this.endereco = new SimpleStringProperty(endereco);
        this.aparelhosLigados = new SimpleIntegerProperty(aparelhosLigados);
        this.aparelhosDesligados = new SimpleIntegerProperty(aparelhosDesligados);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getEndereco() {
        return endereco.get();
    }

    public int getAparelhosLigados() {
        return aparelhosLigados.get();
    }

    public int getAparelhosDesligados() {
        return aparelhosDesligados.get();
    }

    // Setters
    public void setAparelhosLigados(int value) {
        this.aparelhosLigados.set(value);
    }

    public void setAparelhosDesligados(int value) {
        this.aparelhosDesligados.set(value);
    }

    // Property Getters
    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty enderecoProperty() {
        return endereco;
    }

    public SimpleIntegerProperty aparelhosLigadosProperty() {
        return aparelhosLigados;
    }

    public SimpleIntegerProperty aparelhosDesligadosProperty() {
        return aparelhosDesligados;
    }

}
