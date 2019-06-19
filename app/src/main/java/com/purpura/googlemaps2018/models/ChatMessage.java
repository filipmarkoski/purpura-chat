package com.purpura.googlemaps2018.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Date;

public class ChatMessage implements Parcelable {

    private User user;
    private String message;
    private String message_id;
    private String imageUrl;
    private Date timestamp;

    private Bitmap image;

    public ChatMessage() {

    }

    public ChatMessage(User user, String message, String message_id, String imageUrl, Date timestamp) {
        this.user = user;
        this.message = message;
        this.message_id = message_id;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;

    }

    public ChatMessage(Parcel in) {
        this.user = (User) in.readValue(User.class.getClassLoader());
        this.message = in.readString();
        this.message_id = in.readString();
        this.imageUrl = in.readString();
        this.timestamp = (Date) in.readValue(Date.class.getClassLoader());

    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.user);
        dest.writeString(this.message);
        dest.writeString(this.message_id);
        dest.writeString(this.imageUrl);
        dest.writeValue(this.timestamp);

    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean hasMessage() {
        return this.message != null && !this.message.isEmpty();
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasImageUrl() {
        return this.imageUrl != null && !this.imageUrl.isEmpty();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", message_id='" + message_id + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
