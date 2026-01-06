package br.com.fivecom.litoralfm.chromecast;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.MediaMetadata;

//TODO Media Options: Set player values
public class MediaOptions {

    private String image, title, subtitle, media;
    private int mediaType;
    private String contentType;

    public static final int MEDIA_TV = MediaMetadata.MEDIA_TYPE_TV_SHOW;
    public static final int MEDIA_MOVIE = MediaMetadata.MEDIA_TYPE_MOVIE;
    public static final int MEDIA_GENERIC = MediaMetadata.MEDIA_TYPE_GENERIC;
    public static final int MEDIA_MUSIC = MediaMetadata.MEDIA_TYPE_MUSIC_TRACK;

    public static final String TYPE_MUSIC = "audio/*";
    public static final String TYPE_VIDEO = "video/*";

    public MediaOptions(String image, String title, String subtitle, String media, int mediaType, String contentType) {
        this.image = image;
        this.title = title;
        this.subtitle = subtitle;
        this.media = media;
        this.mediaType = mediaType;
        this.contentType = contentType;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getMedia() {
        return media;
    }

    public int getMediaType() {
        return mediaType;
    }

    public String getContentType() {
        return contentType;
    }

    public static class Buider {
        private String image;
        private String title, subtitle, media;
        private int mediaType;
        private String contentType;

        public Buider setImage(@NonNull String image) {
            this.image = image;
            return this;
        }

        public Buider setTitle(@NonNull String title) {
            this.title = title;
            return this;
        }

        public Buider setSubtitle(@NonNull String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Buider setMedia(@NonNull String media) {
            this.media = media;
            return this;
        }

        public Buider setMediaType(int mediaType) throws IllegalArgumentException {
            switch (mediaType) {
                case MEDIA_GENERIC:
                case MEDIA_MOVIE:
                case MEDIA_MUSIC:
                case MEDIA_TV:
                    this.mediaType = mediaType;
                    return this;
                default:
                    throw new IllegalArgumentException("Use MedioOptions.MEDIA_GENERIC or MediaOptions.MEDIA_MOVIE or " +
                            "MedioOptions.MEDIA_TV or MedioOptions.MEDIA_MUSIC");
            }
        }

        public Buider setContentType(@NonNull String contentType) throws IllegalArgumentException {
            switch (contentType) {
                case TYPE_MUSIC:
                case TYPE_VIDEO:
                    this.contentType = contentType;
                    return this;
                default:
                    throw new IllegalArgumentException("Use MedioOptions.TYPE_VIDEO or MediaOptions.TYPE_MUSIC");
            }
        }

        public MediaOptions build() {
            return new MediaOptions(image, title, subtitle, media, mediaType, contentType);
        }
    }

}
