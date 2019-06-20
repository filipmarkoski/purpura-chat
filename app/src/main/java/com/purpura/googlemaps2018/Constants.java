package com.purpura.googlemaps2018;

public class Constants {

    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int ENABLE_GPS_REQUEST = 9003;

    // Image-related permissions
    public static final int PERMISSIONS_REQUEST_CAMERA = 9004;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9005;
    public static final int PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 9006;

    /**
     * the source claims that READ_EXTERNAL_STORAGE needn't be mentioned when permission-asking
     * https://stackoverflow.com/a/31176114/3950168
     */

    public static final int SELECT_GALLERY_IMAGE_REQUEST = 1;
    public static final int CAPTURE_CAMERA_IMAGE_REQUEST = 2;
    public static final int SELECT_CHATROOM_IMAGE_REQUEST = 3;

    public static final int SELECT_THEME = 4;
    public static final int GOOGLE_SIGN_IN = 5;
    /*
     * Default values
     * */
    public static final String DEFAULT_PICTURE_NAME = "example.jpg";
    public static final String DEFAULT_CHATROOM_IMAGE_URL = "http://clv.h-cdn.co/assets/15/22/768x768/square-1432664914-strawberry-facts1.jpg";


    /*
     * The oldest person ever whose age has been independently verified is
     * Jeanne Calment (1875–1997) of France,
     * who lived to the age of 122 years, 164 days.
     * */
    public static final Integer DEFAULT_CHATROOM_AGE_FROM = 0;
    public static final Integer DEFAULT_CHATROOM_AGE_TO = 130;
}
