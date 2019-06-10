package com.purpura.googlemaps2018.models;

public interface ChatInterface {
    Boolean isPrivate();
    Boolean canAccess(String userEmail);
}
