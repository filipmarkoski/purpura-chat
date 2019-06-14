package com.purpura.googlemaps2018.models;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class ChatMessage {

    private User user;
    private String message;
    private String message_id;
    private String imageUrl;
    private @ServerTimestamp
    Date timestamp;

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
}
