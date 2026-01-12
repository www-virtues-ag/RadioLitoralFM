package br.com.fivecom.litoralfm.models.social;

import java.util.ArrayList;
import java.util.List;

public class SocialFeedResponse {
    private double mergedAt;
    private List<String> schema;
    private List<SocialPost> items;

    public SocialFeedResponse() {}

    public double getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(double mergedAt) {
        this.mergedAt = mergedAt;
    }

    public List<String> getSchema() {
        return schema;
    }

    public void setSchema(List<String> schema) {
        this.schema = schema;
    }

    public List<SocialPost> getItems() {
        return items;
    }

    public void setItems(List<SocialPost> items) {
        this.items = items;
    }

    /**
     * Filtra os itens por plataforma
     */
    public List<SocialPost> getFilteredItems(Platform platform) {
        if (items == null || platform == null) {
            return items != null ? items : new ArrayList<>();
        }
        
        List<SocialPost> filtered = new ArrayList<>();
        for (SocialPost post : items) {
            if (post.getPlatform() != null && 
                post.getPlatform().equalsIgnoreCase(platform.name())) {
                filtered.add(post);
            }
        }
        return filtered;
    }
}