package com.purpura.googlemaps2018;

import android.app.Application;

import com.purpura.googlemaps2018.models.User;


public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
