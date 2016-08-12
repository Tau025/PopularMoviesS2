package com.devtau.popularmoviess2.model;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * Класс содержит в себе все подробности по одной рецензии
 * Class contains all the details on the user review
 */
public class Review implements Parcelable {
    String userName;
    String reviewContent;

    public Review(String userName, String reviewContent) {
        this.userName = userName;
        this.reviewContent = reviewContent;
    }

    public Review(Parcel parcel) {
        userName = parcel.readString();
        reviewContent = parcel.readString();
    }

    //getters
    public String getUserName() {
        return userName;
    }

    public String getReviewContent() {
        return reviewContent;
    }


    //Parcelable methods
    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel parcel) {
            return new Review(parcel);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        parcel.writeString(reviewContent);
    }
}
