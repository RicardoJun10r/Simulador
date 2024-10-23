package com.trabalho.api.Interface;

public abstract class IMySocket {
    
    private int porta;

    private String endereco;

    public IMySocket(String endereco, int porta){
        this.endereco = endereco;
        this.porta = porta;
    }

    public int getPorta() {
        return porta;
    }

    public String getEndereco() {
        return endereco;
    }

}
