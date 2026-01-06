package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo de resposta da nova API de programação
 */
public class ProgramacaoAPIResponse {
    
    @SerializedName("programas")
    private List<ProgramaAPI> programas;

    public ProgramacaoAPIResponse() {}

    public List<ProgramaAPI> getProgramas() {
        return programas;
    }

    public void setProgramas(List<ProgramaAPI> programas) {
        this.programas = programas;
    }
}
