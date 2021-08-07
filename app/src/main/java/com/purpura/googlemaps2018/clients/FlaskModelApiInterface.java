package com.purpura.googlemaps2018.clients;

import com.purpura.googlemaps2018.models.PredictOneResponse;
import com.purpura.googlemaps2018.models.Review;
import com.purpura.googlemaps2018.models.SummarizeRequest;
import com.purpura.googlemaps2018.models.SummarizeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FlaskModelApiInterface {

    @POST("/api/predict_one")
    @FormUrlEncoded
    Call<PredictOneResponse> predictOne(@Field("ReviewText") String ReviewText);


    @GET("/api/learn_one")
    Call<Review> learnOne(@Query("ReviewText") String ReviewText,
                          @Query("ReviewRating") Integer ReviewRating);


    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @POST("/api/summarize")
    Call<SummarizeResponse> summarize(@Body SummarizeRequest body);

    /*
    * @Field("summarizationApproach") String summarizationApproach,
    * @Field("textSegments") List<String> textSegments,
    * @Field("nSentences") Integer nSentences
    * */

}
