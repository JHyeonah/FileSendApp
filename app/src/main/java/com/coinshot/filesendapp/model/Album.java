package com.coinshot.filesendapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Album implements  Serializable {
    int num;
    String uri;
    String fileName;
    String title;
    String content;
    String time;

    public Album(int num, String uri, String  fileName, String title, String content, String time){
        this.num = num;
        this.uri = uri;
        this.fileName = fileName;
        this.title = title;
        this.content = content;
        this.time = time;
    }
/*
    @Override
    public int compareTo(Album o) {
        return o.num.compareTo(this.num);
    }*/

    public int getNum(){
        return num;
    }

    public String getUri() {
        return uri;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }
}
