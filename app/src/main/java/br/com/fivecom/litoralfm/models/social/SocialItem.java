package br.com.fivecom.litoralfm.models.social;

import java.util.List;

public class SocialItem {
    private int id;
    private SocialPlatform platform;
    private String url;
    private String text;
    private long postedAt;
    private List<String> mediaUrls;

    public SocialItem() {}

    public SocialItem(int id, SocialPlatform platform, String url, String text, long postedAt, List<String> mediaUrls) {
        this.id = id;
        this.platform = platform;
        this.url = url;
        this.text = text;
        this.postedAt = postedAt;
        this.mediaUrls = mediaUrls;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SocialPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(SocialPlatform platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(long postedAt) {
        this.postedAt = postedAt;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}
