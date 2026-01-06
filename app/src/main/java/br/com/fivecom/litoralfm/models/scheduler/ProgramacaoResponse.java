package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de resposta da API de programação legada
 */
public class ProgramacaoResponse {
    
    @SerializedName("data_extracao")
    private String dataExtracao;
    
    @SerializedName("dias_semana")
    private DiasSemana diasSemana;

    public ProgramacaoResponse() {}

    public String getDataExtracao() {
        return dataExtracao;
    }

    public void setDataExtracao(String dataExtracao) {
        this.dataExtracao = dataExtracao;
    }

    public DiasSemana getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(DiasSemana diasSemana) {
        this.diasSemana = diasSemana;
    }
}
