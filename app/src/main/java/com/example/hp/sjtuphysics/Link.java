package com.example.hp.sjtuphysics;

import android.os.Parcel;
import android.os.Parcelable;

public class Link implements Parcelable {

    private String name;
    private String url;

    public Link(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return this.name;
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
        out.writeString(url);
    }

    public static final Parcelable.Creator<Link> CREATOR = new Creator<Link>() {
        @Override
        public Link createFromParcel(Parcel source) {
            return new Link(source);
        }

        @Override
        public Link[] newArray(int size) {
            return new Link[size];
        }
    };

    public Link(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
    }
}
