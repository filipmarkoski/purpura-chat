package com.purpura.googlemaps2018.models;

import java.io.Serializable;

import android.arch.persistence.room.ColumnInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Review implements Serializable {

    @Expose @ColumnInfo @SerializedName("ReviewText") private String ReviewText;
    @Expose @ColumnInfo @SerializedName("ReviewRating") private String ReviewRating;
    @Expose @ColumnInfo @SerializedName("PredictedReviewRating") private String PredictedReviewRating;

    public Review(String reviewText) {
        this.ReviewText = reviewText;
    }

    public String getReviewText() {
        return ReviewText;
    }

    public void setReviewText(String reviewText) {
        ReviewText = reviewText;
    }

    public String getReviewRating() {
        return ReviewRating;
    }

    public void setReviewRating(String reviewRating) {
        ReviewRating = reviewRating;
    }

    public String getPredictedReviewRating() {
        return PredictedReviewRating;
    }

    public void setPredictedReviewRating(String predictedReviewRating) {
        PredictedReviewRating = predictedReviewRating;
    }

    @Override
    public String toString() {
        return "Review{" + System.lineSeparator()+
                "ReviewText='" + ReviewText + System.lineSeparator()  +
                "ReviewRating='" + ReviewRating + System.lineSeparator()  +
                "PredictedReviewRating='" + PredictedReviewRating + System.lineSeparator()  +
                '}';
    }


}
