package br.com.fivecom.litoralfm.models.locutores;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Classe que representa a resposta da API de locutores
 */
public class LocutorResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private List<LocutorData> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<LocutorData> getData() {
        return data;
    }

    public void setData(List<LocutorData> data) {
        this.data = data;
    }

    /**
     * Classe interna que representa cada locutor na resposta da API
     */
    public static class LocutorData {
        @SerializedName("locutor")
        private String locutor;

        @SerializedName("imagem")
        private String imagem;

        @SerializedName("instagram")
        private String instagram;

        @SerializedName("facebook")
        private String facebook;

        @SerializedName("whatsapp")
        private String whatsapp;

        @SerializedName("descricao")
        private String descricao;

        public String getLocutor() {
            return locutor;
        }

        public void setLocutor(String locutor) {
            this.locutor = locutor;
        }

        public String getFoto() {
            return imagem;
        }

        public void setFoto(String imagem) {
            this.imagem = imagem;
        }

        public String getInstagram() {
            return instagram;
        }

        public void setInstagram(String instagram) {
            this.instagram = instagram;
        }

        public String getFacebook() {
            return facebook;
        }

        public void setFacebook(String facebook) {
            this.facebook = facebook;
        }

        public String getWhatsapp() {
            return whatsapp;
        }

        public void setWhatsapp(String whatsapp) {
            this.whatsapp = whatsapp;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
    }
}
