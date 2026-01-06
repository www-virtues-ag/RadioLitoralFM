package br.com.fivecom.litoralfm.news;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedItem {
    
    @SerializedName("headline")
    public String headline;
    
    @SerializedName("description")
    public String description;
    
    @SerializedName("body")
    public List<BodyElement> body;
    
    @SerializedName("published")
    public String published;
    
    @SerializedName("image")
    public String image;
    
    @SerializedName("shareLink")
    public String shareLink;
    
    @SerializedName("section")
    public String section;
    
    // Getters para compatibilidade
    public String getHeadline() {
        return headline;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<BodyElement> getBody() {
        return body;
    }
    
    public String getPublished() {
        return published;
    }
    
    public String getImage() {
        return image;
    }
    
    public String getShareLink() {
        return shareLink;
    }
    
    public String getSection() {
        return section;
    }
}
