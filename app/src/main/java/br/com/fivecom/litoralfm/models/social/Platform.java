package br.com.fivecom.litoralfm.models.social;

public enum Platform {
    facebook,
    youtube,
    instagram;

    public String getDisplayName() {
        switch (this) {
            case facebook:
                return "Facebook";
            case instagram:
                return "Instagram";
            case youtube:
                return "YouTube";
            default:
                return name();
        }
    }

    public int getColor() {
        switch (this) {
            case facebook:
                return 0xFF1877F2;
            case instagram:
                return 0xFFE4405F;
            default:
                return 0xFF000000;
        }
    }

    public static Platform fromString(String platformName) {
        if (platformName == null) return null;

        String normalized = platformName.toLowerCase().trim();

        for (Platform platform : Platform.values()) {
            if (platform.name().equalsIgnoreCase(normalized)) {
                return platform;
            }
        }

        return null;
    }

    public static boolean isValid(String platformName) {
        return fromString(platformName) != null;
    }
}
