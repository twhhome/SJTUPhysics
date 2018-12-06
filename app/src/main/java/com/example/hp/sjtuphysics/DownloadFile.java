package com.example.hp.sjtuphysics;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadFile implements Parcelable {

    private String filePath;
    private String fileName;
    private String fileSize;
    private String fileType;

    public DownloadFile(String filePath, String fileName, String fileSize, String fileType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFileSize() {
        return this.fileSize;
    }

    public String getFileType() {
        return this.fileType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(filePath);
        out.writeString(fileName);
        out.writeString(fileSize);
        out.writeString(fileType);
    }

    public static final Parcelable.Creator<DownloadFile> CREATOR = new Creator<DownloadFile>() {
        @Override
        public DownloadFile createFromParcel(Parcel source) {
            return new DownloadFile(source);
        }

        @Override
        public DownloadFile[] newArray(int size) {
            return new DownloadFile[size];
        }
    };

    public DownloadFile(Parcel in) {
        this.filePath = in.readString();
        this.fileName = in.readString();
        this.fileSize = in.readString();
        this.fileType = in.readString();
    }
}
