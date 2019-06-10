package com.purpura.googlemaps2018.models;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Chatroom implements Parcelable, ChatInterface {


    private String title;
    private String chatroom_id;
    private Boolean isPrivate;
    ArrayList<UserSetting> users;
    public Chatroom(String title, String chatroom_id, Boolean isPrivate) {
        this.title = title;
        this.chatroom_id = chatroom_id;
        this.isPrivate = isPrivate;
        users = new ArrayList<>();
    }

    public Chatroom() {
        users = new ArrayList<>();
        isPrivate = false;

    }

    protected Chatroom(Parcel in) {
        title = in.readString();
        chatroom_id = in.readString();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "title='" + title + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(chatroom_id);
    }

    @Override
    public Boolean isPrivate() {
        return isPrivate;
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public Boolean canAccess(final String userEmail) {
        return !isPrivate || getSettingForEmail(userEmail)!=null;
    }

    public ArrayList<UserSetting> getUsers() {
        return users;
    }

    public void addUser (User user) {
        users.add( new UserSetting(user,false));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public UserSetting getSettingForEmail (String email){
        return users.stream().filter(userSetting -> userSetting.getUser().getEmail().equals(email)).findAny().orElse(null);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void enableUserLocation (User user){

        Objects.requireNonNull(getSettingForEmail(user.getEmail())).setEnableSharingLocation(true);
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public void resetUsers () {
        //users.clear();
        users = new ArrayList<>();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<User> getUsersAsList(){
        return (ArrayList<User>) users.stream().map(UserSetting::getUser).collect(Collectors.toList());
    }

}
