package br.com.fivecom.litoralfm.models.social;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SocialPost {
    private String platform;
    private String url;
    private String text;
    private long postedAt;
    private List<String> mediaUrls;
    private int id;

    public SocialPost() {
        this.mediaUrls = new ArrayList<>();
    }

    public SocialPost(String platform, String url, String text, long postedAt, List<String> mediaUrls, int id) {
        this.platform = platform;
        this.url = url;
        this.text = text;
        this.postedAt = postedAt;
        this.mediaUrls = mediaUrls != null ? mediaUrls : new ArrayList<>();
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean hasMedia() {
        return mediaUrls != null && !mediaUrls.isEmpty();
    }

    public String getFirstMediaUrl() {
        return hasMedia() ? mediaUrls.get(0) : null;
    }

    public int getMediaCount() {
        return mediaUrls != null ? mediaUrls.size() : 0;
    }

    public Platform getPlatformEnum() {
        if ("facebook".equalsIgnoreCase(platform)) {
            return Platform.facebook;
        } else if ("instagram".equalsIgnoreCase(platform)) {
            return Platform.instagram;
        } else if ("youtube".equalsIgnoreCase(platform)) {
            return Platform.youtube;
        } else if ("x".equalsIgnoreCase(platform) || "twitter".equalsIgnoreCase(platform)) {
            return Platform.x;
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "SocialPost{" +
                "platform='" + platform + '\'' +
                ", text='" + (text != null ? text.substring(0, Math.min(50, text.length())) : "null") + '\'' +
                ", mediaCount=" + getMediaCount() +
                ", id=" + id +
                '}';
    }
}