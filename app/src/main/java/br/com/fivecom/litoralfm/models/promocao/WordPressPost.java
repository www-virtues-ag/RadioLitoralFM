package br.com.fivecom.litoralfm.models.promocao;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Modelo para receber dados da API WordPress
 */
public class WordPressPost implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @SerializedName("id")
    public int id;
    
    @SerializedName("date")
    public String date;
    
    @SerializedName("link")
    public String link;
    
    @SerializedName("title")
    public Title title;
    
    @SerializedName("excerpt")
    public Excerpt excerpt;
    
    @SerializedName("featured_media")
    public int featuredMedia;
    
    public static class Title implements Serializable {
        @SerializedName("rendered")
        public String rendered;
    }
    
    public static class Excerpt implements Serializable {
        @SerializedName("rendered")
        public String rendered;
    }
}

