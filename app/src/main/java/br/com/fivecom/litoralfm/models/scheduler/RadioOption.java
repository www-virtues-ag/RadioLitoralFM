package br.com.fivecom.litoralfm.models.scheduler;

import java.util.Arrays;
import java.util.List;

/**
 * Modelo para opção de rádio
 */
public class RadioOption {
    
    private String id;
    private String nome;

    public RadioOption(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Lista de todas as rádios disponíveis
     */
    public static List<RadioOption> getTodas() {
        return Arrays.asList(
            new RadioOption("10223", "Vitória"),
            new RadioOption("10225", "Noroeste"),
            new RadioOption("10226", "Norte"),
            new RadioOption("10224", "Sul")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RadioOption that = (RadioOption) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
}
