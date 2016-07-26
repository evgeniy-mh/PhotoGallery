package com.mh.evgeniy.photogallery;

import com.google.gson.annotations.SerializedName;

/**
 * Created by evgeniy on 25.07.2016.
 */
public class GalleryItem {

    @SerializedName("title")
    public String mCaption;

    @SerializedName("id")
    public String mId;

    @SerializedName("url_s")
    public String mUrl;


    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString(){
        return mCaption;
    }

}
