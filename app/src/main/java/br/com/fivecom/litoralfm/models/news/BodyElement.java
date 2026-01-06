package br.com.fivecom.litoralfm.models.news;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class BodyElement {
    
    @SerializedName("data")
    private Map<String, Object> data;
    
    // Getter
    public Map<String, Object> getData() {
        return data;
    }
}
