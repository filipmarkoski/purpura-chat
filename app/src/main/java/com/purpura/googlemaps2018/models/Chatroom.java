package com.purpura.googlemaps2018.models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;
import com.purpura.googlemaps2018.Constants;

import java.util.ArrayList;

public class Chatroom implements Parcelable {

    private String title;
    private String chatroom_id;
    private Boolean isPrivate;

    private String imageUrl;
    private Integer ageFrom;
    private Integer ageTo;
    private Double predictedReviewRatingAverage;

    /*
     * this.users should be this.userSettings!!!
     * but shouldn't be modified due to database stubbornness
     * */
    private ArrayList<UserSetting> users;

    private GeoPoint geoPoint;
    private Boolean isShowingNearby;
    private float radiusInMeters;
    private String theme;

    @SuppressWarnings("unchecked")
    protected Chatroom(Parcel in) {
        title = in.readString();
        chatroom_id = in.readString();
        users = in.readArrayList(UserSetting.class.getClassLoader());
        isPrivate = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.imageUrl = in.readString();
        this.ageFrom = in.readInt();
        this.ageTo = in.readInt();
        isShowingNearby = (Boolean) in.readValue(Boolean.class.getClassLoader());
        radiusInMeters = in.readFloat();
        theme = in.readString();
        Double lat = null;
        Double lon = null;
        if (isShowingNearby) {
            lat = (Double) in.readValue(Double.class.getClassLoader());
            lon = (Double) in.readValue(Double.class.getClassLoader());

        }
        if (lat != null && lon != null)
            geoPoint = new GeoPoint(lat, lon);
        this.predictedReviewRatingAverage = in.readDouble();
    }

    public String getTheme() {
        return theme;
    }

    public Chatroom() {
        this.predictedReviewRatingAverage = -1.0;
        this.users = new ArrayList<>();
        this.isPrivate = false;
        this.isShowingNearby = false;
        this.imageUrl = "http://clv.h-cdn.co/assets/15/22/768x768/square-1432664914-strawberry-facts1.jpg";
        /*this.ageFrom = Constants.DEFAULT_CHATROOM_AGE_FROM;
        this.ageTo = Constants.DEFAULT_CHATROOM_AGE_TO;*/
    }

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

    public void setTheme(String theme) {
        this.theme = theme;
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
        dest.writeString(this.imageUrl);
        if (this.ageFrom != null) {
            dest.writeInt(this.ageFrom);
        } else {
            dest.writeInt(Constants.DEFAULT_CHATROOM_AGE_FROM);
        }
        if (this.ageTo != null) {
            dest.writeInt(this.ageTo);
        } else {
            dest.writeInt(Constants.DEFAULT_CHATROOM_AGE_TO);
        }
        dest.writeValue(isShowingNearby);
        dest.writeFloat(radiusInMeters);
        dest.writeString(theme);
        if (isShowingNearby && geoPoint != null) {
            dest.writeValue(geoPoint.getLatitude());
            dest.writeValue(geoPoint.getLongitude());
        } else {
            dest.writeValue(null);
            dest.writeValue(null);
        }
        dest.writeDouble(predictedReviewRatingAverage);
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


    public Double getPredictedReviewRatingAverage() {
        return predictedReviewRatingAverage;
    }

    public void setPredictedReviewRatingAverage(Double predictedReviewRatingAverage) {
        this.predictedReviewRatingAverage = predictedReviewRatingAverage;
    }

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


    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setUsers(ArrayList<UserSetting> users) {
        this.users = users;
    }

    public Boolean isAccessable(final String userEmail) {
        // user can access the chatroom if the chatroom is Public or
        // if user has been added in the chatroom and exists in the settings
        Boolean isPublicNonBusiness = this.isPublic() && !isBusiness();
        Boolean isPrivateAndInclusive = (Boolean) (this.isPrivate && this.getSettingForEmail(userEmail) != null);

        return isPublicNonBusiness || isPrivateAndInclusive;
    }

    public Boolean isPublic() {
        return !this.isPrivate;
    }

    public Boolean isBusiness() {
        return this.isShowingNearby && this.geoPoint != null;
    }

    public Boolean isPublicAndBusiness() {
        return this.isPublic() && this.isBusiness();
    }

    public ArrayList<UserSetting> getUsers() {
        return users;
    }


    public void addUser(User user) {
        if (getSettingForEmail(user.getEmail()) == null) {
            this.users.add(new UserSetting(user, false));
        }
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

    public Boolean getIsUserSharingLocation(User user) {
        if (user == null) return null;
        UserSetting userSetting = getSettingForEmail(user.getEmail());
        if (userSetting != null) {
            return userSetting.getEnableSharingLocation();
        }
        return null;
    }

    public void setPrivate(Boolean aPrivate) {
        this.isPrivate = aPrivate;
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

    public Boolean checkProximity(GeoPoint geoPoint) {
        if (isShowingNearby == null || geoPoint == null || !hasLocation()) {
            return false;
        }
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

    /* Image-related */

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasImageUrl() {
        return this.imageUrl != null && !this.imageUrl.isEmpty();
    }

    /* Age-related */

    public Integer getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(Integer ageFrom) {
        this.ageFrom = ageFrom;
    }

    public Integer getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(Integer ageTo) {
        this.ageTo = ageTo;
    }

    public Boolean hasAgeLimits() {
        return this.ageFrom != null && this.ageTo != null;
    }

    public Boolean isInAgeRangeInclusive(Integer currentUserAge) {
        if (currentUserAge != null && this.hasAgeLimits()) {
            return this.ageFrom <= currentUserAge && currentUserAge <= this.ageTo;
        }
        return true;
    }

    public void changeUserNickname(String email, String enteredNickname) {
        UserSetting userSetting = getSettingForEmail(email);
        if (userSetting != null)
            userSetting.setUserNickname(enteredNickname);
    }

    public String getUserNickname(String email) {
        UserSetting userSetting = getSettingForEmail(email);
        if (userSetting != null)
            return userSetting.getUserNickname();
        else return null;
    }
}
