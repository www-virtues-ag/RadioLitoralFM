package br.com.fivecom.litoralfm.services.metadata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EntryAlbum {
    @SerializedName("resultCount")
    public int resultCount;
    @SerializedName("results")
    public List<Result> results;

    public class Result {
        @SerializedName("wrapperType")
        public String wrapperType;
        @SerializedName("artistId")
        public int artistId;
        @SerializedName("collectionId")
        public int collectionId;
        @SerializedName("artistName")
        public String artistName;
        @SerializedName("collectionName")
        public String collectionName;
        @SerializedName("collectionCensoredName")
        public String collectionCensoredName;
        @SerializedName("artistViewUrl")
        public String artistViewUrl;
        @SerializedName("collectionViewUrl")
        public String collectionViewUrl;
        @SerializedName("artworkUrl60")
        public String artworkUrl60;
        @SerializedName("artworkUrl100")
        public String artworkUrl100;
        @SerializedName("collectionPrice")
        public double collectionPrice;
        @SerializedName("collectionExplicitness")
        public String collectionExplicitness;
        @SerializedName("trackCount")
        public int trackCount;
        @SerializedName("copyright")
        public String copyright;
        @SerializedName("country")
        public String country;
        @SerializedName("currency")
        public String currency;
        @SerializedName("releaseDate")
        public String releaseDate;
        @SerializedName("primaryGenreName")
        public String primaryGenreName;
        @SerializedName("previewUrl")
        public String previewUrl;
        @SerializedName("description")
        public String description;
    }
}
