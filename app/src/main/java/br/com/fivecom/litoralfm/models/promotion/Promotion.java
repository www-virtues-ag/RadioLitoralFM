package br.com.fivecom.litoralfm.models.promotion;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Promotion implements Parcelable {
    @SerializedName("identificador")
    public String id;
    @SerializedName("titulo")
    public String title;
    @SerializedName("link")
    public String link;
    @SerializedName("vencedor")
    public String winner;
    @SerializedName("imagem")
    public String image;
    @SerializedName("inicio")
    public String inicio;
    @SerializedName("termino")
    public String termino;
    @SerializedName("descricao")
    public String description;
    @SerializedName("sorteio")
    public String sorteio;
    @SerializedName("status")
    public String status;

    protected Promotion(Parcel in) {
        title = in.readString();
        link = in.readString();
        description = in.readString();
        image = in.readString();
        inicio = in.readString();
        termino = in.readString();
        sorteio = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(description);
        dest.writeString(image);
        dest.writeString(inicio);
        dest.writeString(termino);
        dest.writeString(sorteio);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Promotion> CREATOR = new Creator<>() {
        @Override
        public Promotion createFromParcel(Parcel in) {
            return new Promotion(in);
        }

        @Override
        public Promotion[] newArray(int size) {
            return new Promotion[size];
        }
    };
}
