package com.purpura.googlemaps2018.models;

import java.io.Serializable;

import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Parcelable.Creator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// https://www.jsonschema2pojo.org/
public class SummarizeResponse implements Serializable, Parcelable {

    @SerializedName("Method")
    @Expose
    private String method;
    @SerializedName("Summary")
    @Expose
    private String summary;
    @SerializedName("return")
    @Expose
    private Boolean _return;
    public final static Creator<SummarizeResponse> CREATOR = new Creator<SummarizeResponse>() {


        @SuppressWarnings({"unchecked"})
        public SummarizeResponse createFromParcel(android.os.Parcel in) {
            return new SummarizeResponse(in);
        }

        public SummarizeResponse[] newArray(int size) {
            return (new SummarizeResponse[size]);
        }

    };
    private final static long serialVersionUID = 8589313718655520482L;

    protected SummarizeResponse(android.os.Parcel in) {
        this.method = ((String) in.readValue((String.class.getClassLoader())));
        this.summary = ((String) in.readValue((String.class.getClassLoader())));
        this._return = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public SummarizeResponse() {
    }

    /**
     * @param summary
     * @param _return
     * @param method
     */
    public SummarizeResponse(String method, String summary, Boolean _return) {
        super();
        this.method = method;
        this.summary = summary;
        this._return = _return;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public SummarizeResponse withMethod(String method) {
        this.method = method;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public SummarizeResponse withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public Boolean getReturn() {
        return _return;
    }

    public void setReturn(Boolean _return) {
        this._return = _return;
    }

    public SummarizeResponse withReturn(Boolean _return) {
        this._return = _return;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SummarizeResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("method");
        sb.append('=');
        sb.append(((this.method == null) ? "<null>" : this.method));
        sb.append(',');
        sb.append("summary");
        sb.append('=');
        sb.append(((this.summary == null) ? "<null>" : this.summary));
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
        dest.writeValue(summary);
        dest.writeValue(_return);
    }

    public int describeContents() {
        return 0;
    }

}