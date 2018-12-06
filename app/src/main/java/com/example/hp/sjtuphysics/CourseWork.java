package com.example.hp.sjtuphysics;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CourseWork implements Parcelable {

    private String week;
    private String content;
    private Bundle links;

    public CourseWork(String Week, String Content) {
        this.week = Week;
        this.content = Content;
    }

    public CourseWork(String Week, String Content, Bundle Links) {
        this.week = Week;
        this.content = Content;
        this.links = Links;
    }

    public String getWeek() {
        return this.week;
    }

    public String getContent() {
        return this.content;
    }

    public Bundle getLinks() {
        return this.links;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(week);
        out.writeString(content);
        out.writeBundle(links);
    }

    public static final Parcelable.Creator<CourseWork> CREATOR = new Creator<CourseWork>() {
        @Override
        public CourseWork createFromParcel(Parcel source) {
            return new CourseWork(source);
        }

        @Override
        public CourseWork[] newArray(int size) {
            return new CourseWork[size];
        }
    };

    public CourseWork(Parcel in) {
        this.week = in.readString();
        this.content = in.readString();
        this.links = in.readBundle();
    }
}
