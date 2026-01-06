package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Modelo de programa para a nova API
 */
public class ProgramaAPI implements Serializable {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("link")
    private String link;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("hr_inicio")
    private String hrInicio;
    
    @SerializedName("hr_final")
    private String hrFinal;
    
    @SerializedName("nm_locutor")
    private String nmLocutor;
    
    @SerializedName("image")
    private String image;
    
    @SerializedName("nr_dia_semana")
    private String nrDiaSemana;

    public ProgramaAPI() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHrInicio() {
        return hrInicio;
    }

    public void setHrInicio(String hrInicio) {
        this.hrInicio = hrInicio;
    }

    public String getHrFinal() {
        return hrFinal;
    }

    public void setHrFinal(String hrFinal) {
        this.hrFinal = hrFinal;
    }

    public String getNmLocutor() {
        return nmLocutor;
    }

    public void setNmLocutor(String nmLocutor) {
        this.nmLocutor = nmLocutor;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNrDiaSemana() {
        return nrDiaSemana;
    }

    public void setNrDiaSemana(String nrDiaSemana) {
        this.nrDiaSemana = nrDiaSemana;
    }
}
