package com.panicbutton.common;


import android.os.Parcel;
import android.os.Parcelable;

public class DangerZone implements Parcelable {

    public static final String SETTINGS_RADIUS = "dangerZoneRadius";
    public static final String LOCATION = "location";

    private final String id;
    private final double latitude;
    private final double longitude;
    private final int radius;

    public DangerZone(String id, double latitude, double longitude, int radius) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    private DangerZone(Parcel source) {
        this.id = source.readString();
        this.latitude = source.readDouble();
        this.longitude = source.readDouble();
        this.radius= source.readInt();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DangerZone that = (DangerZone) o;

        return id.equals(that.id);
    }

    @Override public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getRadius() {
        return radius;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeDouble(this.latitude);
        parcel.writeDouble(this.longitude);
        parcel.writeInt(this.radius);
    }

    public static final Parcelable.Creator<DangerZone> CREATOR
            = new Parcelable.Creator<DangerZone>() {
        public DangerZone createFromParcel(Parcel in) {
            return new DangerZone(in);
        }

        public DangerZone[] newArray(int size) {
            return new DangerZone[size];
        }
    };


}
