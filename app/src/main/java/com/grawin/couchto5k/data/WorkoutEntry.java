package com.grawin.couchto5k.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by rgraw on 2/23/2016.
 */
public class WorkoutEntry implements Parcelable {
    private String name;
    private int time_sec;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(time_sec);
    }

    // Creator
    public static final Parcelable.Creator<WorkoutEntry> CREATOR =
            new Parcelable.Creator<WorkoutEntry>() {
        @Override
        public WorkoutEntry createFromParcel(Parcel source) {
            return new WorkoutEntry(source);
        }

        @Override
        public WorkoutEntry[] newArray(int size) {
            return new WorkoutEntry[size];
        }
    };

    // De-parcel object
    public WorkoutEntry(Parcel in) {
        name = in.readString();
        time_sec = in.readInt();
    }

    public WorkoutEntry(String name, int time_sec) {
        this.name = name;
        this.time_sec = time_sec;
    }

    public WorkoutEntry(WorkoutEntry entry) {
        this.name = entry.name;
        this.time_sec = entry.time_sec;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime_sec() {
        return time_sec;
    }

    public void setTime_sec(int time_sec) {
        this.time_sec = time_sec;
    }


}
