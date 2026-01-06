package br.com.fivecom.litoralfm.models.promotion;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Promotions {
    @SerializedName("promocoes")
    public List<Promotion> promocoes;
}
