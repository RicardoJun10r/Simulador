package com.trabalho.salaAula.Aparelhos;

import com.trabalho.salaAula.Aparelhos.Interface.InterAparelho;

public class ArCondicionado implements InterAparelho {

    private boolean status;

    private final int ID;
    
    public ArCondicionado(int id){
        this.ID = id;
        this.status = false;
    }
    
    public boolean isStatus() {
        return status;
    }

    public int getID() {
        return ID;
    }
    
    @Override
    public void Ligar() {
        this.status = true;
    }

    @Override
    public void Desligar() {
        this.status = false;
    }

    @Override
    public String Descricao() {
        return "Aparelho: Ar Condicionado " + ID + " - Estado: " + status;
    }

    @Override
    public Boolean State() {
        return status;
    }
    
}
