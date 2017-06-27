package com.alesp.feedbackapp.MetaWear.stickers;

/**
 * Created by Stefano on 09/11/2015.
 */

import android.os.Parcel;
import android.os.Parcelable;


import java.util.Date;

public class MyNearable implements Parcelable, Comparable<MyNearable> {
    private Integer id; // identifier rilevamento Sticker
    private String identifier; // identifier Sticker
    private Integer major;
    private Integer minor;
    private Integer power;
    private Integer rssi;
    private Double temperature;
    private Boolean motion;
    private Double xAcceleration;
    private Double yAcceleration;
    private Double zAcceleration;
    private String orientation;
    private String proximity;
    private Long timestamp;
    private String azione;
    private Date date;


    public MyNearable(com.estimote.sdk.Nearable nearable){
        this.id = null;
        identifier = nearable.identifier;
        major = nearable.region.getMajor();
        minor = nearable.region.getMinor();
        power = nearable.power.powerInDbm;
        temperature = nearable.temperature;
        motion = nearable.isMoving;
        xAcceleration = nearable.xAcceleration;
        yAcceleration = nearable.yAcceleration;
        zAcceleration = nearable.zAcceleration;
        orientation = nearable.orientation.toString();
        rssi = nearable.rssi;
        proximity = Utils.computeProximity(nearable).toString();
        timestamp = System.currentTimeMillis();
        azione = "UNKNOWN";
        date = new Date();

    }

    public void setId(int id){ this.id = id; }

    public void addAction(String action){
        azione = action;
    }

    public Boolean getMotion(){
        return motion;
    }

    public Long getTime(){
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        boolean r = true;
        if(o instanceof MyNearable){
            MyNearable m = (MyNearable)  o;
            if(!m.getId().equals(this.getId()))
                r = false;
            if(m.getZ() != this.getZ())
                r = false;
            if(m.getX() != this.getX())
                r = false;
            if(m.getY() != this.getY())
                r = false;
            if(m.getTime() != m.getTime())
                r = false;
            if(m.getRssi() != m.getRssi())
                r = false;

        } else{
            r = false;
        }
        return r;
    }

    public int getRssi() { return rssi; }
    public String getId(){
        return identifier;
    }

    public double getX(){
        return xAcceleration;
    }
    public double getY(){
        return yAcceleration;
    }
    public double getZ(){
        return zAcceleration;
    }



    protected MyNearable(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        identifier = in.readString();
        major = in.readByte() == 0x00 ? null : in.readInt();
        minor = in.readByte() == 0x00 ? null : in.readInt();
        power = in.readByte() == 0x00 ? null : in.readInt();
        rssi = in.readByte() == 0x00 ? null : in.readInt();
        temperature = in.readByte() == 0x00 ? null : in.readDouble();
        byte motionVal = in.readByte();
        motion = motionVal == 0x02 ? null : motionVal != 0x00;
        xAcceleration = in.readByte() == 0x00 ? null : in.readDouble();
        yAcceleration = in.readByte() == 0x00 ? null : in.readDouble();
        zAcceleration = in.readByte() == 0x00 ? null : in.readDouble();
        orientation = in.readString();
        proximity = in.readString();
        timestamp = in.readByte() == 0x00 ? null : in.readLong();
        azione = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if(id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        dest.writeString(identifier);
        if (major == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(major);
        }
        if (minor == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(minor);
        }
        if (power == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(power);
        }
        if (rssi == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(rssi);
        }
        if (temperature == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(temperature);
        }
        if (motion == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (motion ? 0x01 : 0x00));
        }
        if (xAcceleration == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(xAcceleration);
        }
        if (yAcceleration == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(yAcceleration);
        }
        if (zAcceleration == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(zAcceleration);
        }
        dest.writeString(orientation);
        dest.writeString(proximity);
        if (timestamp == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeLong(timestamp);
        }
        dest.writeString(azione);
        dest.writeLong(date != null ? date.getTime() : -1L);

    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MyNearable> CREATOR = new Parcelable.Creator<MyNearable>() {
        @Override
        public MyNearable createFromParcel(Parcel in) {
            return new MyNearable(in);
        }

        @Override
        public MyNearable[] newArray(int size) {
            return new MyNearable[size];
        }
    };

    public Date getDate() {
        return date;
    }

    public String getAction() {
        return azione;
    }

    @Override
    public int compareTo(MyNearable another) {
        return (this.id+"").compareTo(another+"");
    }

    public String getIdentifier(){
        return identifier;
    }
}