package com.purpura.googlemaps2018.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Objects;

public class User implements Parcelable {

    private String email;
    private String user_id;
    private String username;
    private String avatar;
    private Boolean seeNearbyEnabled;
    private String deviceToken;


    public User(String email, String user_id, String username, String avatar) {
        this.email = email;
        this.user_id = user_id;
        this.username = username;
        this.avatar = avatar;
        this.seeNearbyEnabled = false;

    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        username = in.readString();
        avatar = in.readString();

    }

    public User() {
        this.seeNearbyEnabled = false;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(username);
        dest.writeString(avatar);

    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static Creator<User> getCREATOR() {
        return CREATOR;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.setUsername(email.substring(0, email.indexOf("@")));
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getSeeNearbyEnabled() {
        return seeNearbyEnabled;
    }

    public void toggleSeeNearbyEnabled() {
        if (seeNearbyEnabled != null) {
            seeNearbyEnabled = !seeNearbyEnabled;
        }
    }

    public void setSeeNearbyEnabled(Boolean seeNearbyEnabled) {
        this.seeNearbyEnabled = seeNearbyEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getUser_id(), user.getUser_id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getUser_id());
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }


    private String firstName;
    private String lastName;
    private Integer age;
    private String biography;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}

