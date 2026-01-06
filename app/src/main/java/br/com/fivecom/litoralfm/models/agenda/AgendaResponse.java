package br.com.fivecom.litoralfm.models.agenda;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AgendaResponse {
    
    @SerializedName("agenda")
    public List<AgendaItem> agenda;

    public AgendaResponse() {
    }

    public AgendaResponse(List<AgendaItem> agenda) {
        this.agenda = agenda;
    }
}

