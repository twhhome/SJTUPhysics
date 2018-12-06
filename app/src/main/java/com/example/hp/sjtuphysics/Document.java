package com.example.hp.sjtuphysics;

import android.os.Parcel;
import android.os.Parcelable;

public class Document implements Parcelable {

    private String name;
    private String description;
    private String size;
    private String date;
    private String url;

    public Document(String name, String description, String size, String date, String url) {
        this.name = name;
        this.description = description;
        this.size = size;
        this.date = date;
        this.url = url;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSize() {
        return this.size;
    }

    public String getDate() {
        return this.date;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(description);
        out.writeString(size);
        out.writeString(date);
        out.writeString(url);
    }

    public static final Parcelable.Creator<Document> CREATOR = new Creator<Document>() {
        @Override
        public Document createFromParcel(Parcel source) {
            return new Document(source);
        }

        @Override
        public Document[] newArray(int size) {
            return new Document[size];
        }
    };

    public Document(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.size = in.readString();
        this.date = in.readString();
        this.url = in.readString();
    }
}
