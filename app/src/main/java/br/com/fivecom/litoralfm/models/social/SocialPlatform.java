package br.com.fivecom.litoralfm.models.social;

public enum SocialPlatform {
    INSTAGRAM("instagram", "Instagram", "camera.aperture"),
    TIKTOK("tiktok", "TikTok", "music.note"),
    FACEBOOK("facebook", "Facebook", "f.circle"),
    YOUTUBE("youtube", "YouTube", "play.rectangle.fill"),
    X("x", "X", "xmark");

    private final String id;
    private final String label;
    private final String sfSymbol;

    SocialPlatform(String id, String label, String sfSymbol) {
        this.id = id;
        this.label = label;
        this.sfSymbol = sfSymbol;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getSfSymbol() {
        return sfSymbol;
    }

    public static SocialPlatform fromId(String id) {
        for (SocialPlatform platform : values()) {
            if (platform.id.equals(id)) {
                return platform;
            }
        }
        return INSTAGRAM; // default
    }
}
