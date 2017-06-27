package com.alesp.feedbackapp.MetaWear;

import com.mbientlab.metawear.data.CartesianShort;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by claudio on 07/06/16.
 */
public class AccelerationData {
    private Short axesX;
    private Short axesY;
    private Short axesZ;
    private Date date;
    private long timestamp;

    public AccelerationData(CartesianShort axes, Calendar timestamp) {
        this.axesX = axes.x();
        this.axesY = axes.y();
        this.axesZ = axes.z();
        this.date = timestamp.getTime();
        this.timestamp = timestamp.getTimeInMillis();
    }

    public double euclideanDistance(AccelerationData accelData){

        double distance = -1;

        if(this.timestamp != accelData.getTimestamp()) {
            double xDiff = accelData.getAxesX() - this.axesX;
            double yDiff = accelData.getAxesY() - this.axesY;
            double zDiff = accelData.getAxesZ() - this.axesZ;
            distance = Math.sqrt(xDiff*xDiff + yDiff*yDiff + zDiff*zDiff);
        }
        return distance;
    }

    public double checkMovementOnAxes(AccelerationData previous){
        double asseX = Math.abs(this.getAxesX() - previous.getAxesX());
        double asseY = Math.abs(this.getAxesY() - previous.getAxesY());
        double asseZ = Math.abs(this.getAxesZ() - previous.getAxesZ());
        return Math.min(Math.min(asseX,asseY),asseZ);
    }

    public boolean checkMovement(AccelerationData data, double threshold){
        double asseX = Math.abs(this.getAxesX() - data.getAxesX());
        double asseY = Math.abs(this.getAxesY() - data.getAxesY());
        double asseZ = Math.abs(this.getAxesZ() - data.getAxesZ());
        if( asseX > threshold || asseY > threshold || asseZ > threshold ){
            return true;
        } else {
            return false;
        }
    }

    //valuta gli AccelerationDate con un movimento sotto una certa soglia
    public boolean checkRange(AccelerationData data, double threshold){
        double [] assi = new double[3];
        int count = 0;
        assi [0] = Math.abs(this.getAxesX() - data.getAxesX());
        assi [1] = Math.abs(this.getAxesY() - data.getAxesY());
        assi [2] = Math.abs(this.getAxesZ() - data.getAxesZ());
        for (int i = 0; i < 3 ; i++){
            if(assi[i] < threshold){
                count = count + 1;
            }
        }
        if( count > 0 ){
            return true;
        } else {
            return false;
        }
    }

    private boolean checkMovementOnAxes(double current, double previous, double threshold){
        return Math.abs(current - previous) > threshold;
    }

    public boolean checkIfMoved(AccelerationData previous, double threshold){
        double currentX = this.getAxesX();
        double previousX = previous.getAxesX();
        double currentY = this.getAxesY();
        double previousY = previous.getAxesY();
        double currentZ = this.getAxesZ();
        double previousZ = previous.getAxesZ();

        return checkMovementOnAxes(currentX,previousX,threshold) || checkMovementOnAxes(currentY,previousY,threshold) || checkMovementOnAxes(currentZ,previousZ,threshold);

    }


    public double differenceMoving(AccelerationData previous){

        double diffX = Math.abs(this.getAxesX() - previous.getAxesX());
        double diffY = Math.abs(this.getAxesY() - previous.getAxesY());
        double diffZ = Math.abs(this.getAxesZ() - previous.getAxesZ());

        return (diffX+diffY+diffZ)/3;
    }




    public Short getAxesY() {
        return axesY;
    }

    public Short getAxesZ() {
        return axesZ;
    }

    public Short getAxesX() {
        return axesX;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate(){
        return date;
    }

    public String getDateString(){
        return date.toString();
    }

    public long getTimestampDifference(AccelerationData current){
        return(this.timestamp - current.getTimestamp());
    }

    @Override
    public String toString() {
        return "AccelerationData{" +
                "axesX=" + axesX +
                ", axesY=" + axesY +
                ", axesZ=" + axesZ +
                ", timestamp=" + timestamp +
                '}';
    }
}
