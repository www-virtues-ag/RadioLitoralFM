package br.com.fivecom.litoralfm.models.announcer;

import com.google.gson.annotations.SerializedName;

public class Announcer {
    @SerializedName("locutor")
    public String locutor;
    @SerializedName("imagem")
    public String imagem;
    @SerializedName("instagram")
    public String instagram;
    @SerializedName("facebook")
    public String facebook;
    @SerializedName("whatsapp")
    public String whatsapp;
}
