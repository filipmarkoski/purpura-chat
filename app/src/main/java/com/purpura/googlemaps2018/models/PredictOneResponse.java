package com.purpura.googlemaps2018.models;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// https://www.jsonschema2pojo.org/
public class PredictOneResponse implements Serializable, Parcelable {

    @SerializedName("Method")
    @Expose
    private String method;
    @SerializedName("PredictedReviewRating")
    @Expose
    private Double predictedReviewRating;
    @SerializedName("return")
    @Expose
    private Boolean _return;
    public final static Creator<PredictOneResponse> CREATOR = new Creator<PredictOneResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public PredictOneResponse createFromParcel(android.os.Parcel in) {
            return new PredictOneResponse(in);
        }

        public PredictOneResponse[] newArray(int size) {
            return (new PredictOneResponse[size]);
        }

    };
    private final static long serialVersionUID = 1450984687357294988L;

    protected PredictOneResponse(android.os.Parcel in) {
        this.method = ((String) in.readValue((String.class.getClassLoader())));
        this.predictedReviewRating = ((Double) in.readValue((Double.class.getClassLoader())));
        this._return = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public PredictOneResponse() {
    }

    /**
     * @param _return
     * @param method
     * @param predictedReviewRating
     */
    public PredictOneResponse(String method, Double predictedReviewRating, Boolean _return) {
        super();
        this.method = method;
        this.predictedReviewRating = predictedReviewRating;
        this._return = _return;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public PredictOneResponse withMethod(String method) {
        this.method = method;
        return this;
    }

    public Double getPredictedReviewRating() {
        return predictedReviewRating;
    }

    public void setPredictedReviewRating(Double predictedReviewRating) {
        this.predictedReviewRating = predictedReviewRating;
    }

    public PredictOneResponse withPredictedReviewRating(Double predictedReviewRating) {
        this.predictedReviewRating = predictedReviewRating;
        return this;
    }

    public Boolean getReturn() {
        return _return;
    }

    public void setReturn(Boolean _return) {
        this._return = _return;
    }

    public PredictOneResponse withReturn(Boolean _return) {
        this._return = _return;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PredictOneResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("method");
        sb.append('=');
        sb.append(((this.method == null) ? "<null>" : this.method));
        sb.append(',');
        sb.append("predictedReviewRating");
        sb.append('=');
        sb.append(((this.predictedReviewRating == null) ? "<null>" : this.predictedReviewRating));
        sb.append(',');
        sb.append("_return");
        sb.append('=');
        sb.append(((this._return == null) ? "<null>" : this._return));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(method);
        dest.writeValue(predictedReviewRating);
        dest.writeValue(_return);
    }

    public int describeContents() {
        return 0;
    }

}
