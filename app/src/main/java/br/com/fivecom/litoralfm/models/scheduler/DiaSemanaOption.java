package br.com.fivecom.litoralfm.models.scheduler;

import java.util.Arrays;
import java.util.List;

/**
 * Modelo para opção de dia da semana
 */
public class DiaSemanaOption {
    
    private String id;
    private String nome;
    private String nomeCompleto;

    public DiaSemanaOption(String id, String nome, String nomeCompleto) {
        this.id = id;
        this.nome = nome;
        this.nomeCompleto = nomeCompleto;
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

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    /**
     * Lista de todos os dias da semana disponíveis
     */
    public static List<DiaSemanaOption> getTodos() {
        return Arrays.asList(
            new DiaSemanaOption("", "TODOS", "Todos os dias"),
            new DiaSemanaOption("segunda", "SEG", "Segunda-feira"),
            new DiaSemanaOption("terca", "TER", "Terça-feira"),
            new DiaSemanaOption("quarta", "QUA", "Quarta-feira"),
            new DiaSemanaOption("quinta", "QUI", "Quinta-feira"),
            new DiaSemanaOption("sexta", "SEX", "Sexta-feira"),
            new DiaSemanaOption("sabado", "SÁB", "Sábado"),
            new DiaSemanaOption("domingo", "DOM", "Domingo")
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiaSemanaOption that = (DiaSemanaOption) o;
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
