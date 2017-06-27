package com.alesp.feedbackapp.MetaWear;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by claudio on 23/06/16.
 */
public class MetaWearManipulation {
    private String macAddress;
    private List<Double> listX;
    private List<Double> listY;
    private List<Double> listZ;
    private List<Long> listTimestamp;

    public MetaWearManipulation(String macAddress) {
        this.macAddress = macAddress;
        listX = new ArrayList<>();
        listY = new ArrayList<>();
        listZ = new ArrayList<>();
        listTimestamp = new ArrayList<>();
    }

    public void clearManipulation(){
        listX.clear();
        listY.clear();
        listZ.clear();
        listTimestamp.clear();
    }

    public void addAccelerationData(AccelerationData accelerationData){

        if(accelerationData !=null) {

            listX.add(accelerationData.getAxesX().doubleValue());
            listY.add(accelerationData.getAxesY().doubleValue());
            listZ.add(accelerationData.getAxesZ().doubleValue());
            listTimestamp.add(accelerationData.getTimestamp());

        }
    }

    public long getDuration(){
        return listTimestamp.get(listTimestamp.size()-1) - listTimestamp.get(0);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public List<Double> getListX() {
        return listX;
    }

    public List<Double> getListY() {
        return listY;
    }

    public List<Double> getListZ() {
        return listZ;
    }

    public String toString(){
        return("MAC: "+macAddress +"\n\t"+ " ListX: "+listX+ "\n\t"+" ListY: "+listY+"\n\t"+ " ListZ: "+listZ);
    }
}
