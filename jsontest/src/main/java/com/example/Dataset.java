package com.example;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by evgeniy on 26.07.2016.
 */
public class Dataset {
    public String album_id;
    public String album_title;

    @SerializedName("album_images")
    List<AlbumImages> images=new ArrayList<AlbumImages>();

}
