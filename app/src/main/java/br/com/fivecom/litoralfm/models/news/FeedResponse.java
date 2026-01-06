// FeedResponse.java
package br.com.fivecom.litoralfm.models.news;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedResponse {

    @SerializedName("item")
    private List<FeedItem> item;

    public List<FeedItem> getItem() {
        return item;
    }

    public void setItem(List<FeedItem> item) {
        this.item = item;
    }
}
