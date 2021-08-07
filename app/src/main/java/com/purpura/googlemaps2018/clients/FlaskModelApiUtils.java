package com.purpura.googlemaps2018.clients;

public class FlaskModelApiUtils {

    private FlaskModelApiUtils() {}

    public static final String BASE_URL = "http://londonrestaurantthesisappservice.azurewebsites.net/";

    public static FlaskModelApiInterface getAPIService() {
        return FlaskModelApiClient.getRetrofit(BASE_URL).create(FlaskModelApiInterface.class);
    }
}
