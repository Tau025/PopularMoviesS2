package com.devtau.popularmoviess2.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Объекты этого класса являются частью листа внутри фильма
 * Objects of this class are part of list in every movie
 */
public class Trailer implements Parcelable {
    private String source;
    private String name;
    private int size;
    private String type;

    public Trailer(String source, String name, int size, String type) {
        this.source = source;
        this.name = name;
        this.size = size;
        this.type = type;
    }

    public Trailer(Parcel parcel) {
        source = parcel.readString();
        name = parcel.readString();
        size = parcel.readInt();
        type = parcel.readString();
    }


    //getters
    public String getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public String getTypeSize() {
        return type + " - " + String.valueOf(size);
    }


    //Parcelable methods
    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel parcel) {
            return new Trailer(parcel);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(source);
        parcel.writeString(name);
        parcel.writeInt(size);
        parcel.writeString(type);
    }
}
