package com.purpura.googlemaps2018.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;

public class ChatroomClusterMarker implements ClusterItem {

    private LatLng position; // required field
    private String title; // required field
    private String snippet; // required field

    private GeoPoint geoPoint;
    private Chatroom chatroom;

    public ChatroomClusterMarker(Chatroom chatroom) {
        this.chatroom = chatroom;
        this.geoPoint = chatroom.getGeoPoint();
        this.position = new LatLng(this.geoPoint.getLatitude(), this.geoPoint.getLongitude());
        this.title = chatroom.getTitle();
        this.snippet = String.format("%1.3f", chatroom.getPredictedReviewRatingAverage());
     }

    public Chatroom getChatroom() {
        return chatroom;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

}
