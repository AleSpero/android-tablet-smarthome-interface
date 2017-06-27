package com.alesp.feedbackapp.MetaWear.stickers;

import com.estimote.sdk.Nearable;

import java.util.Date;

/**
 * Created by Stefano on 15/12/2015.
 */

public class Temperature {
    private String identifier;
    private Long time;
    private Date date;
    private Double temperature;
    private Boolean refrigerated;

    public Temperature(Nearable n){
        identifier = n.identifier;
        time = System.currentTimeMillis();
        date = new Date();
        temperature = n.temperature;
        if(temperature >= 21.0)
            refrigerated = false;
        else
            refrigerated = true;
    }

    public Temperature setRefrigerated(Nearable n, Boolean refrigerated) {
        update(n);
        this.refrigerated = refrigerated;
        return this;
    }

    public Boolean getRefrigerated(){
        return refrigerated;
    }

    public String getIdentifier(){
        return identifier;
    }

    public Long getTime(){ return time; }

    public Double getTemperature(){
        return temperature;
    }

    public void update(Nearable n){
        time = System.currentTimeMillis();
        date = new Date();
        temperature = n.temperature;
    }

    @Override
    public boolean equals(Object o){
        if (o == null)return false;
        if (!(o instanceof Temperature))return false;
        Temperature t = (Temperature)o;
        return t.getIdentifier().equals(((Temperature) o).getIdentifier());
    }
}
