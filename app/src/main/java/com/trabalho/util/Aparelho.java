package com.trabalho.util;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Aparelho {

    private SimpleIntegerProperty id;
    private SimpleStringProperty endereco;
    private SimpleIntegerProperty aparelhosLigados;
    private SimpleIntegerProperty aparelhosDesligados;

    public Aparelho(int id, String endereco, int aparelhosLigados, int aparelhosDesligados) {
        this.id = new SimpleIntegerProperty(id);
        this.endereco = new SimpleStringProperty(endereco);
        this.aparelhosLigados = new SimpleIntegerProperty(aparelhosLigados);
        this.aparelhosDesligados = new SimpleIntegerProperty(aparelhosDesligados);
    }

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

    public void setAparelhosLigados(int value) {
        this.aparelhosLigados.set(value);
    }

    public void setAparelhosDesligados(int value) {
        this.aparelhosDesligados.set(value);
    }

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
