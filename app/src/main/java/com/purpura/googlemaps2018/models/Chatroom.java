package com.purpura.googlemaps2018.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Chatroom implements Parcelable {

    private String title;
    private String chatroom_id;
    private Boolean isPrivate;
    private GeoPoint geoPoint;
    private Boolean isShowingNearby;
    private ArrayList<UserSetting> users;
    private float radiusInMeters;

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public float getRadiusInMeters() {
        return radiusInMeters;
    }

    public void setRadiusInMeters(float radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
    }

    public Boolean getIsShowingNearby() {
        return isShowingNearby;
    }

    public void setIsShowingNearby(Boolean showingNearby) {
        isShowingNearby = showingNearby;
    }

    public Chatroom(String title, String chatroom_id, Boolean isPrivate, Boolean isShowingNearby) {
        this.title = title;
        this.chatroom_id = chatroom_id;
        this.isPrivate = isPrivate;
        this.isShowingNearby = isShowingNearby;
        users = new ArrayList<>();
    }

    public Chatroom() {
        users = new ArrayList<>();
        isPrivate = false;
        isShowingNearby = false;

    }

    @SuppressWarnings("unchecked")
    protected Chatroom(Parcel in) {
        title = in.readString();
        chatroom_id = in.readString();
        users = in.readArrayList(UserSetting.class.getClassLoader());
        isPrivate = (Boolean) in.readValue(Boolean.class.getClassLoader());
        isShowingNearby = (Boolean) in.readValue(Boolean.class.getClassLoader());
        radiusInMeters = in.readFloat();
        Double lat=null;
        Double lon=null;
        if (isShowingNearby) {
            lat = (Double) in.readValue(Double.class.getClassLoader());
            lon = (Double) in.readValue(Double.class.getClassLoader());

        }
        if(lat!=null&&lon!=null)
        geoPoint = new GeoPoint(lat, lon);
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

    @NonNull
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
        dest.writeValue(isShowingNearby);
        dest.writeFloat(radiusInMeters);
        if (isShowingNearby && geoPoint!=null) {
            dest.writeValue(geoPoint.getLatitude());
            dest.writeValue(geoPoint.getLongitude());
        } else {
            dest.writeValue(null);
            dest.writeValue(null);
        }

    }


    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setUsers(ArrayList<UserSetting> users) {
        this.users = users;
    }


    public Boolean canAccess(final String userEmail) {
        return !isPrivate || getSettingForEmail(userEmail) != null;
    }

    public ArrayList<UserSetting> getUsers() {
        return users;
    }


    public void addUser(User user) {
        if (getSettingForEmail(user.getEmail()) == null)
            users.add(new UserSetting(user, false));

    }


    public UserSetting getSettingForEmail(String email) {
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

    public void resetUsers() {
        users = new ArrayList<>();
    }

    public void removeUser(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null)
            users.remove(userSetting);
    }

    public Boolean hasLocation() {
        return this.geoPoint != null;
    }

    public boolean checkProximity(GeoPoint geoPoint) {
        if (isShowingNearby == null || geoPoint == null || !hasLocation())
            return false;
        Location chatroomLocation = toLocation(this.geoPoint);
        Location userLocation = toLocation(geoPoint);
        return isShowingNearby && chatroomLocation.distanceTo(userLocation) < radiusInMeters;
    }

    private Location toLocation(GeoPoint geoPoint) {
        Location location = new Location("");
        location.setLatitude(geoPoint.getLatitude());
        location.setLongitude(geoPoint.getLongitude());
        return location;

    }

    public void enableUserLocation(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null) {
            userSetting.setEnableSharingLocation(true);
        }
    }

    public void disableUserLocation(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null) {
            userSetting.setEnableSharingLocation(false);
        }
    }

    public Boolean isLocationEnabled(User user) {
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null) {
            return userSetting.getEnableSharingLocation();
        }
        return false;
    }
}
