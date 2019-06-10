package com.purpura.googlemaps2018.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSetting implements Parcelable {
    User user;
    Boolean enableSharingLocation;

    public UserSetting() {
        user = new User();
        enableSharingLocation = false;
    }

    public UserSetting(Parcel in) {
        user = (User) in.readValue(User.class.getClassLoader());
        enableSharingLocation = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getEnableSharingLocation() {
        return enableSharingLocation;
    }

    public void setEnableSharingLocation(Boolean enableSharingLocation) {
        this.enableSharingLocation = enableSharingLocation;
    }

    public UserSetting(User user, Boolean enableSharingLocation) {
        this.user = user;
        this.enableSharingLocation = enableSharingLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(enableSharingLocation);
    }
    public static final Creator<UserSetting> CREATOR = new Creator<UserSetting>() {
        @Override
        public UserSetting createFromParcel(Parcel in) {
            return new UserSetting(in);
        }

        @Override
        public UserSetting[] newArray(int size) {
            return new UserSetting[size];
        }
    };
}
