package br.com.fivecom.litoralfm.models.notification;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.onesignal.notifications.INotification;

public class Notification implements Parcelable {
    public String title, image, link, description;

    public Notification(@NonNull INotification notification) {
        title = notification.getTitle();
        image = notification.getBigPicture();
        link = notification.getLaunchURL();
        description = notification.getBody();
    }

    protected Notification(Parcel in) {
        title = in.readString();
        image = in.readString();
        link = in.readString();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(image);
        dest.writeString(link);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}