package br.com.fivecom.litoralfm.models.email;

import com.google.gson.annotations.SerializedName;

public class Email {
    @SerializedName("erro")
    public int erro;
    @SerializedName("mensagem")
    public String mensagem;
}
