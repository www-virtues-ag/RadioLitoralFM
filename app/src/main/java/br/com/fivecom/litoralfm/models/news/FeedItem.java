package br.com.fivecom.litoralfm.models.news;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class FeedItem {
    
    @SerializedName("headline")
    private String headline;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("body")
    private List<BodyElement> body;
    
    @SerializedName("published")
    private String published;
    
    @SerializedName("image")
    private String image;
    
    @SerializedName("shareLink")
    private String shareLink;
    
    @SerializedName("section")
    private String section;
    
    // Getters
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
