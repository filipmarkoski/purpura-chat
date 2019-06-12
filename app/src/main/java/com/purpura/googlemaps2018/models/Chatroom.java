package com.purpura.googlemaps2018.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Chatroom implements Parcelable {


    private String title;
    private String chatroom_id;
    private Boolean isPrivate;
    private ArrayList<UserSetting> users;
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
        users = in.readArrayList(UserSetting.class.getClassLoader());
        isPrivate = (Boolean) in.readValue(Boolean.class.getClassLoader());
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
        dest.writeList(users);
        dest.writeValue(isPrivate);
    }


    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setUsers(ArrayList<UserSetting> users) {
        this.users = users;
    }


    public Boolean canAccess(final String userEmail) {
        return !isPrivate || getSettingForEmail(userEmail)!=null;
    }

    public ArrayList<UserSetting> getUsers() {
        return users;
    }


    public void addUser (User user) {
        if (getSettingForEmail(user.getEmail()) == null)
            users.add(new UserSetting(user, false));

    }


    public UserSetting getSettingForEmail (String email){
        for (UserSetting userSetting : users) {
            if (userSetting.getUser().getEmail().equals(email)) {
                return userSetting;
            }
        }
        return null;
    }

    public Boolean toggleUserLocation(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null) {
            userSetting.setEnableSharingLocation(!userSetting.getEnableSharingLocation());
            return userSetting.getEnableSharingLocation();
        }
        return null;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public void resetUsers () {
        //users.clear();
        users = new ArrayList<>();
    }


    public void removeUser(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null)
            users.remove(userSetting);
    }
}
