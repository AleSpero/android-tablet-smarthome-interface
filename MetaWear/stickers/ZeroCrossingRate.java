package com.alesp.feedbackapp.MetaWear.stickers;

import java.util.List;

/**
 * Created by Stefano on 18/01/2016.
 */

public class ZeroCrossingRate {
    private List<Double> signals;
    private double lengthInSecond;

    /**
     * Constructor
     *
     * @param signals       input signal array
     * @param lengthInSecond        length of the signal (in second)
     */
    public ZeroCrossingRate(List<Double> signals, double lengthInSecond){
        setSignals(signals,1);
    }

    /**
     * set the signals
     *
     * @param signals       input signal array
     * @param lengthInSecond        length of the signal (in second)
     */
    public void setSignals(List<Double> signals, double lengthInSecond){
        this.signals=signals;
        this.lengthInSecond=lengthInSecond;
    }

    public double evaluate(){
        int numZC=0;
        int size=signals.size();

        for (int i=0; i<size-1; i++){
            if((signals.get(i)>=0 && signals.get(i+1)<0) || (signals.get(i)<0 && signals.get(i+1)>=0)){
                numZC++;
            }
        }
        return numZC/lengthInSecond;
    }

}
