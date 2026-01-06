package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de programa para a API legada (programacao.json)
 */
public class Programa {
    
    @SerializedName("horario")
    private String horario;
    
    @SerializedName("nome")
    private String nome;
    
    @SerializedName("apresentadores")
    private String apresentadores;

    public Programa() {}

    public Programa(String horario, String nome, String apresentadores) {
        this.horario = horario;
        this.nome = nome;
        this.apresentadores = apresentadores;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApresentadores() {
        return apresentadores;
    }

    public void setApresentadores(String apresentadores) {
        this.apresentadores = apresentadores;
    }
}
