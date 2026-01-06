package br.com.fivecom.litoralfm.news;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class BodyElement {
    
    @SerializedName("data")
    public Map<String, Object> data;
    
    // Getter para compatibilidade
    public Map<String, Object> getData() {
        return data;
    }
}
