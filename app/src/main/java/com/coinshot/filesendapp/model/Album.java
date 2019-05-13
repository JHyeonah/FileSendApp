package com.coinshot.filesendapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Album implements Comparable<Album>, Serializable {
    String num;
    String uri;
    String fileName;
    String title;
    String content;
    String time;

    public Album(String num, String uri, String  fileName, String title, String content, String time){
        this.num = num;
        this.uri = uri;
        this.fileName = fileName;
        this.title = title;
        this.content = content;
        this.time = time;
    }
/*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.num);
        dest.writeString(this.uri);
        dest.writeString(this.fileName);
        dest.writeString(this.title);
        dest.writeString(this.content);
        dest.writeString(this.time);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album();
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
*/
    @Override
    public int compareTo(Album o) {
        return o.num.compareTo(this.num);
    }

    public String getNum(){
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
