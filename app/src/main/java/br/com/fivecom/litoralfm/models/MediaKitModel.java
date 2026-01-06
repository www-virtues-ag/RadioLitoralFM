package br.com.fivecom.litoralfm.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaKitModel {

    @SerializedName("customer")
    private String customer;

    @SerializedName("metadata")
    private Metadata metadata;

    @SerializedName("playStore")
    private PlayStore playStore;

    @SerializedName("instagram")
    private Instagram instagram;

    @SerializedName("youtube")
    private Youtube youtube;

    @SerializedName("facebook")
    private Facebook facebook;

    public String getCustomer() { return customer; }
    public Metadata getMetadata() { return metadata; }
    public PlayStore getPlayStore() { return playStore; }
    public Instagram getInstagram() { return instagram; }
    public Youtube getYoutube() { return youtube; }
    public Facebook getFacebook() { return facebook; }

    public static class Metadata {
        @SerializedName("scrape_date")
        private String scrapeDate;

        @SerializedName("source")
        private String source;

        @SerializedName("version")
        private String version;

        public String getScrapeDate() { return scrapeDate; }
        public String getSource() { return source; }
        public String getVersion() { return version; }
    }

    public static class PlayStore {
        @SerializedName("apps")
        private List<App> apps;

        public List<App> getApps() { return apps; }

        public static class App {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("downloads")
            private int downloads;

            @SerializedName("stars")
            private double stars;

            @SerializedName("reviews")
            private int reviews;

            public String getId() { return id; }
            public String getName() { return name; }
            public int getDownloads() { return downloads; }
            public double getStars() { return stars; }
            public int getReviews() { return reviews; }
        }
    }

    public static class Instagram {
        @SerializedName("profiles")
        private List<Profile> profiles;

        public List<Profile> getProfiles() { return profiles; }

        public static class Profile {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("followers")
            private int followers;

            @SerializedName("likes")
            private int likes;

            @SerializedName("followerPercentage")
            private double followerPercentage;

            public String getId() { return id; }
            public String getName() { return name; }
            public int getFollowers() { return followers; }
            public int getLikes() { return likes; }
            public double getFollowerPercentage() { return followerPercentage; }
        }
    }

    public static class Youtube {
        @SerializedName("channels")
        private List<Channel> channels;

        public List<Channel> getChannels() { return channels; }

        public static class Channel {
            @SerializedName("id")
            private String id;

            @SerializedName("name")
            private String name;

            @SerializedName("subscribers")
            private int subscribers;

            @SerializedName("views")
            private int views;

            @SerializedName("followerPercentage")
            private double followerPercentage;

            public String getId() { return id; }
            public String getName() { return name; }
            public int getSubscribers() { return subscribers; }
            public int getViews() { return views; }
            public double getFollowerPercentage() { return followerPercentage; }
        }
    }

    public static class Facebook {
        @SerializedName("pages")
        private List<Page> pages;

        public List<Page> getPages() { return pages; }

        public static class Page {
            @SerializedName("url")
            private String url;

            @SerializedName("pageType")
            private String pageType;

            @SerializedName("likes")
            private Integer likes;

            @SerializedName("followers")
            private Integer followers;

            @SerializedName("name")
            private String name;

            @SerializedName("followerPercentage")
            private double followerPercentage;

            public String getUrl() { return url; }
            public String getPageType() { return pageType; }
            public Integer getLikes() { return likes; }
            public Integer getFollowers() { return followers; }
            public String getName() { return name; }
            public double getFollowerPercentage() { return followerPercentage; }
        }
    }
}
