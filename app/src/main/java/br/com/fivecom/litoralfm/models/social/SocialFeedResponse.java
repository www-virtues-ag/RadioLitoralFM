package br.com.fivecom.litoralfm.models.social;

import java.util.List;

public class SocialFeedResponse {
    private double mergedAt;
    private List<String> schema;
    private List<SocialItem> items;

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

    public List<SocialItem> getItems() {
        return items;
    }

    public void setItems(List<SocialItem> items) {
        this.items = items;
    }
}