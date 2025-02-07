package com.trabalho.salaAula;

import java.util.ArrayList;
import java.util.List;

import com.trabalho.salaAula.Aparelhos.ArCondicionado;
import com.trabalho.salaAula.Aparelhos.Computador;
import com.trabalho.salaAula.Aparelhos.Interface.InterAparelho;

public class Sala {

    private List<InterAparelho> aparelhos;

    private final int NUMERO_APARELHOS;

    public Sala(int numero_aparelhos){
        this.aparelhos = new ArrayList<>();
        this.NUMERO_APARELHOS = numero_aparelhos;
    }

    public void preencher(){
        for(int i = 0; i < NUMERO_APARELHOS; i++){
            this.aparelhos.add(new Computador(i));
            this.aparelhos.add(new ArCondicionado(i));
        }
    }

    public String desligarAparelhos(){
        for(InterAparelho i : aparelhos)
            i.Desligar();
        return "Aparelhos desligados!";
    }

    public String ligarAparelhos(){
        for(InterAparelho i : aparelhos)
            i.Ligar();
        return "Aparelhos ligados!";
    }

    public String mostrarAparelhos(){
        int totalLigados = 0;
        int totalDesligados = 0;
    
        for(InterAparelho i : aparelhos) {
            if(i.State()) {
                totalLigados++;
            } else {
                totalDesligados++;
            }
        }
    
        String mensagem = "Sala com [ " + (NUMERO_APARELHOS * 2) + " ] aparelhos*";
        mensagem += "Ligados: " + totalLigados + "*";
        mensagem += "Desligados: " + totalDesligados + "*";
        return mensagem;
    }

}
