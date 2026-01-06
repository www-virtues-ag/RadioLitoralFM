package br.com.fivecom.litoralfm.models.announcer;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Announcers {
    @SerializedName("status")
    public String status;
    @SerializedName("data")
    public List<Announcer> announcer;
}
