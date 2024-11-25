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
    
    public SimpleIntegerProperty getId() {
        return id;
    }
    
    public void setId(SimpleIntegerProperty id) {
        this.id = id;
    }
    
    public SimpleStringProperty getEndereco() {
        return endereco;
    }
    
    public void setEndereco(SimpleStringProperty endereco) {
        this.endereco = endereco;
    }
    
    public SimpleIntegerProperty getAparelhosLigados() {
        return aparelhosLigados;
    }
    
    public void setAparelhosLigados(SimpleIntegerProperty aparelhosLigados) {
        this.aparelhosLigados = aparelhosLigados;
    }
    
    public SimpleIntegerProperty getAparelhosDesligados() {
        return aparelhosDesligados;
    }
    
    public void setAparelhosDesligados(SimpleIntegerProperty aparelhosDesligados) {
        this.aparelhosDesligados = aparelhosDesligados;
    }

    
}
