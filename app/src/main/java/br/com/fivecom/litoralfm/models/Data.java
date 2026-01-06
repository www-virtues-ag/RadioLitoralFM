package br.com.fivecom.litoralfm.models;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import br.com.fivecom.litoralfm.BuildConfig;

public class Data {
    @SerializedName("license")
    public boolean license;
    @SerializedName("id_app")
    public String id_app;
    @SerializedName("token")
    public String token;
    @SerializedName("id_google_cast")
    public String id_google_cast;
    @SerializedName("redirect_android")
    public String redirect;
    @SerializedName("polices_android")
    public String polices;
    @SerializedName("radios")
    public List<Radios> radios;

    public static class Builder {
        public Data build() {
            return new Gson().fromJson(FirebaseRemoteConfig.getInstance().getString(BuildConfig.PACKAGE), Data.class);
        }
    }

    public class Radios {
        @SerializedName("id")
        public String id;
        @SerializedName("name")
        public String name;
        @SerializedName("url_site")
        public String url_site;
        @SerializedName("url_cbox")
        public String url_cbox;
        @SerializedName("url_stream_audio")
        public String url_stream_audio;
        @SerializedName("url_stream_audio_secondary")
        public String url_stream_audio_secondary;
        @SerializedName("url_stream_video")
        public String url_stream_video;
        @SerializedName("address")
        public String address;
        @SerializedName("facebook")
        public List<Links> facebook;
        @SerializedName("whatsapp")
        public List<Links> whatsapp;
        @SerializedName("tunein")
        public List<Links> tunein;
        @SerializedName("twitter")
        public List<Links> twitter;
        @SerializedName("instagram")
        public List<Links> instagram;
        @SerializedName("youtube")
        public List<Links> youtube;
        @SerializedName("url_magazine")
        public String url_magazine;
        @SerializedName("spotify")
        public List<Links> spotify;
        @SerializedName("phone_number")
        public String phone_number;
        @SerializedName("url_cast_image")
        public String url_cast_image;
    }

    public class Links {
        @SerializedName("url")
        public String url;
        @SerializedName("scheme")
        public String scheme;
        @SerializedName("type")
        public String type;
        @SerializedName("social")
        public String social;
    }


}
