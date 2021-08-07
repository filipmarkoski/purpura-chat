package com.purpura.googlemaps2018.models;

import java.io.Serializable;
import java.util.List;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// https://www.jsonschema2pojo.org/
public class SummarizeRequest implements Serializable, Parcelable
{

    @SerializedName("summarizationApproach")
    @Expose
    private String summarizationApproach;
    @SerializedName("textSegments")
    @Expose
    private List<String> textSegments = null;
    @SerializedName("nSentences")
    @Expose
    private Integer nSentences;
    public final static Creator<SummarizeRequest> CREATOR = new Creator<SummarizeRequest>() {


        @SuppressWarnings({
                "unchecked"
        })
        public SummarizeRequest createFromParcel(android.os.Parcel in) {
            return new SummarizeRequest(in);
        }

        public SummarizeRequest[] newArray(int size) {
            return (new SummarizeRequest[size]);
        }

    }
            ;
    private final static long serialVersionUID = -7213208355298550436L;

    protected SummarizeRequest(android.os.Parcel in) {
        this.summarizationApproach = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.textSegments, (java.lang.String.class.getClassLoader()));
        this.nSentences = ((Integer) in.readValue((Integer.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public SummarizeRequest() {
    }

    /**
     *
     * @param summarizationApproach
     * @param nSentences
     * @param textSegments
     */
    public SummarizeRequest(String summarizationApproach, List<String> textSegments, Integer nSentences) {
        super();
        this.summarizationApproach = summarizationApproach;
        this.textSegments = textSegments;
        this.nSentences = nSentences;
    }

    public String getSummarizationApproach() {
        return summarizationApproach;
    }

    public void setSummarizationApproach(String summarizationApproach) {
        this.summarizationApproach = summarizationApproach;
    }

    public SummarizeRequest withSummarizationApproach(String summarizationApproach) {
        this.summarizationApproach = summarizationApproach;
        return this;
    }

    public List<String> getTextSegments() {
        return textSegments;
    }

    public void setTextSegments(List<String> textSegments) {
        this.textSegments = textSegments;
    }

    public SummarizeRequest withTextSegments(List<String> textSegments) {
        this.textSegments = textSegments;
        return this;
    }

    public Integer getnSentences() {
        return nSentences;
    }

    public void setnSentences(Integer nSentences) {
        this.nSentences = nSentences;
    }

    public SummarizeRequest withnSentences(Integer nSentences) {
        this.nSentences = nSentences;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SummarizeRequest.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("summarizationApproach");
        sb.append('=');
        sb.append(((this.summarizationApproach == null)?"<null>":this.summarizationApproach));
        sb.append(',');
        sb.append("textSegments");
        sb.append('=');
        sb.append(((this.textSegments == null)?"<null>":this.textSegments));
        sb.append(',');
        sb.append("nSentences");
        sb.append('=');
        sb.append(((this.nSentences == null)?"<null>":this.nSentences));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(summarizationApproach);
        dest.writeList(textSegments);
        dest.writeValue(nSentences);
    }

    public int describeContents() {
        return 0;
    }

}
