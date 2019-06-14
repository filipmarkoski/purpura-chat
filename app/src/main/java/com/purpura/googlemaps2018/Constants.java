package com.purpura.googlemaps2018;

public class Constants {

    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;

    // Image-related permissions
    public static final int PERMISSIONS_REQUEST_CAMERA = 9004;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 9005;

    /**
     * the source claims that READ_EXTERNAL_STORAGE needn't be mentioned when permission-asking
     * https://stackoverflow.com/a/31176114/3950168
     */

    public static final int SELECT_GALLERY_IMAGE_REQUEST = 1;
    public static final int CAPTURE_CAMERA_IMAGE_REQUEST = 2;
    public static final String DEFAULT_PICTURE_NAME = "example.jpg";
}
