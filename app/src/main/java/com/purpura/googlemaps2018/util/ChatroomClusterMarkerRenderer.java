package com.purpura.googlemaps2018.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.purpura.googlemaps2018.R;
import com.purpura.googlemaps2018.models.ChatroomClusterMarker;

import java.util.ArrayList;
import java.util.Collection;

public class ChatroomClusterMarkerRenderer extends DefaultClusterRenderer<ChatroomClusterMarker> {

    private final IconGenerator mClusterIconGenerator;

    public ChatroomClusterMarkerRenderer(Context context, GoogleMap map,
                                         ClusterManager<ChatroomClusterMarker> clusterManager) {
        super(context, map, clusterManager);
        mClusterIconGenerator = new IconGenerator(context.getApplicationContext());
        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.ic_message_black_24dp);
        clusterIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);
        mClusterIconGenerator.setBackground(clusterIcon);
    }

    @Override
    protected void onBeforeClusterItemRendered(ChatroomClusterMarker item,
                                               MarkerOptions markerOptions) {

        Double predictedReviewRatingAverage = item.getChatroom().getPredictedReviewRatingAverage();

        Float markerHue = BitmapDescriptorFactory.HUE_MAGENTA;
        if (predictedReviewRatingAverage >= 4.5) {
            markerHue = BitmapDescriptorFactory.HUE_GREEN;
        } else if (4.0 <= predictedReviewRatingAverage && predictedReviewRatingAverage < 4.5) {
            markerHue = BitmapDescriptorFactory.HUE_AZURE;
        } else if (3.5 <= predictedReviewRatingAverage && predictedReviewRatingAverage < 4.0) {
            markerHue = BitmapDescriptorFactory.HUE_ORANGE;
        } else if (0 <= predictedReviewRatingAverage && predictedReviewRatingAverage < 3.5) {
            markerHue = BitmapDescriptorFactory.HUE_RED;
        }

        BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(markerHue);

        markerOptions.icon(markerDescriptor);
    }

    @Override
    protected void onClusterItemRendered(ChatroomClusterMarker clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ChatroomClusterMarker> cluster, MarkerOptions markerOptions){

        //modify padding for one or two digit numbers
        if (cluster.getSize() < 10) {
            mClusterIconGenerator.setContentPadding(40, 20, 0, 0);
        }
        else {
            mClusterIconGenerator.setContentPadding(30, 20, 0, 0);
        }

        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }
}
