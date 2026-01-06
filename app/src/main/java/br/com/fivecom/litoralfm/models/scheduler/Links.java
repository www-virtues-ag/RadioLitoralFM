package br.com.fivecom.litoralfm.models.scheduler;

import com.google.gson.annotations.SerializedName;

public class Links {
    @SerializedName("url")
    public String url;
    @SerializedName("scheme")
    public String scheme;
    @SerializedName("type")
    public String type;
    @SerializedName("social")
    public String social;
}
