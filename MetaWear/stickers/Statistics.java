package com.alesp.feedbackapp.MetaWear.stickers;

import com.alesp.feedbackapp.MetaWear.MetaWearManipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;

/**
 * Created by Stefano on 18/11/2015.
 */

public class Statistics {
    List<Double> dataX = new ArrayList<>();
    List<Double> dataY = new ArrayList<>();
    List<Double> dataZ = new ArrayList<>();
    int size;
    long durata;

    public Statistics(List<Double> dataX, List<Double> dataY, List<Double> dataZ) {
        this.dataX = dataX;
        this.dataY = dataY;
        this.dataZ = dataZ;
        size = dataX.size();
    }

    public Statistics(List<Double> dataX, List<Double> dataY, List<Double> dataZ, Long durata){
        this(dataX, dataY, dataZ);
        this.durata = durata;
    }

    public Statistics(List<MyNearable> listN, Long durata){
        this(listN);
        this.durata = durata;
    }

    public Statistics(List<MyNearable> listN){
        for(MyNearable n : listN){
            dataX.add(n.getX());
            dataY.add(n.getY());
            dataZ.add(n.getZ());
        }
        size = listN.size();
    }

    double getMean(String s) {
        double sum = 0.0;
        if(s.equals("x")){
            for(double a : dataX)
                sum += a;
        } else if(s.equals("y")){
            for(double a : dataY)
                sum += a;
        } else{
            for(double a : dataZ)
                sum += a;
        }
        return sum/ size;
    }

    double getVariance(String s) {
        double temp = 0;
        if(s.equals("x")){
            double mean = getMean("x");
            for(double a : dataX)
                temp += (a-mean)*(a-mean);
        } else if(s.equals("y")){
            double mean = getMean("y");
            for(double a : dataY)
                temp += (a-mean)*(a-mean);
        } else{
            double mean = getMean("z");
            for(double a : dataZ)
                temp += (a-mean)*(a-mean);
        }
        return temp/size;
    }

    double getCovariance(String s1, String s2) {
        double temp = 0;
        if(s1.equals("x") && s2.equals("y")) {
            double meanX = getMean("x");
            double meanY = getMean("y");
            for(int i=0; i< size; i++){
                temp += (meanX - dataX.get(i))*(meanY - dataY.get(i));
            }
        } else if(s1.equals("x") && s2.equals("z")){
            double meanX = getMean("x");
            double meanZ = getMean("z");
            for(int i=0; i< size; i++){
                temp += (meanX - dataX.get(i))*(meanZ - dataZ.get(i));
            }
        } else {
            double meanY = getMean("y");
            double meanZ = getMean("z");
            for(int i=0; i< size; i++){
                temp += (meanY - dataY.get(i))*(meanZ - dataZ.get(i));
            }
        }
        return temp/size;
        //return new BigDecimal(temp/size).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    double getPearson(String s1, String s2){
        double ret = 0;
        if(s1.equals("x") && s2.equals("y")) {
            ret = (getCovariance("x", "y")/(getVariance("x")*getVariance("y")));
        } else if(s1.equals("x") && s2.equals("z")){
            ret = (getCovariance("x", "z")/(getVariance("x")*getVariance("z")));
        } else {
            ret = (getCovariance("y", "z")/(getVariance("y")*getVariance("z")));
        }
        return String.valueOf(ret).equals("NaN") ? 0 : ret;
    }

    double getStdDev(String s) {
        if(s.equals("x")){
            return Math.sqrt(getVariance("x"));
        } else if(s.equals("y")){
            return Math.sqrt(getVariance("y"));
        } else{
            return Math.sqrt(getVariance("z"));
        }
    }

    double getMax(String s){
        double max = 0;
        if(s.equals("x")){
            max = dataX.get(0);
            for(double tmp : dataX){
                if(tmp > max){
                    max = tmp;
                }
            }
        } else if(s.equals("y")){
            max = dataY.get(0);
            for(double tmp : dataY){
                if(tmp > max){
                    max = tmp;
                }
            }
        } else{
            max = dataZ.get(0);
            for(double tmp : dataZ){
                if(tmp > max){
                    max = tmp;
                }
            }
        }
        return max;
    }

    double getMin(String s){
        double min = 0;
        if(s.equals("x")){
            min = dataX.get(0);
            for(double tmp : dataX){
                if(tmp < min){
                    min = tmp;
                }
            }
        } else if(s.equals("y")){
            min = dataY.get(0);
            for(double tmp : dataY){
                if(tmp < min){
                    min = tmp;
                }
            }
        } else{
            min = dataZ.get(0);
            for(double tmp : dataZ){
                if(tmp < min){
                    min = tmp;
                }
            }
        }
        return min;
    }

    double getDifference(String s){
        if(s.equals("x")){
            return getMax("x")-getMin("x");
        } else if(s.equals("y")){
            return getMax("y")-getMin("y");
        } else{
            return getMax("z")-getMin("z");
        }
    }

    double getConsecutivi(String s){
        double temp = 0;
        double prec = 0;
        if(s.equals("x")){
            for(double a : dataX){
                if(a != dataX.get(0))
                    temp += Math.abs(a - prec);
                prec = a;
            }
        } else if(s.equals("y")){
            for(double a : dataY){
                if(a != dataY.get(0))
                    temp += Math.abs(a - prec);
                prec = a;
            }
        } else{
            for(double a : dataZ){
                if(a != dataZ.get(0))
                    temp += Math.abs(a - prec);
                prec = a;
            }
        }
        return temp/size;
    }

    double getZeroCrossing(String s){
        ZeroCrossingRate zcr;
        if(s.equals("x")){
            zcr = new ZeroCrossingRate(dataX, durata/1000);
        } else if(s.equals("y")){
            zcr = new ZeroCrossingRate(dataY, durata/1000);
        } else{
            zcr = new ZeroCrossingRate(dataZ, durata/1000);
        }
        double r= zcr.evaluate();
        return String.valueOf(r).equals("NaN") ? 0 : r;
    }

    double getEnergy(String s){
        double[] inreal = new double[dataX.size()];
        double[] inimag = new double[dataX.size()];
        double[] outreal = new double[dataX.size()];
        double[] outimag = new double[dataX.size()];
        if(s.equals("x")){
            for(int i=0; i<dataX.size(); i++){
                inreal[i] = dataX.get(i);
                inimag[i] = 0.0;
            }
        } else if(s.equals("y")){
            for(int i=0; i<dataY.size(); i++){
                inreal[i] = dataY.get(i);
                inimag[i] = 0.0;
            }
        } else{
            for(int i=0; i<dataZ.size(); i++){
                inreal[i] = dataZ.get(i);
                inimag[i] = 0.0;
            }
        }
        Dft.computeDft(inreal, inimag, outreal, outimag);

        double res = 0.0;
        for(int i=0; i< size; i++){
            res += ((outreal[i])*(outreal[i])+(outimag[i])*(outimag[i]))*((outreal[i])*(outreal[i])+(outimag[i])*(outimag[i]));
        }


        double r= res/size;
        return String.valueOf(r).equals("NaN") ? 0 : r;
    }

    double getRootMeanSquare(String s){
        double res = 0.0;
        if(s.equals("x")){
            for(double a : dataX)
                res += a*a;
        } else if(s.equals("y")){
            for(double a : dataY)
                res += a*a;
        } else{
            for(double a : dataZ)
                res += a*a;
        }
        double r= Math.sqrt(res/size);
        return String.valueOf(r).equals("NaN") ? 0 : r;

    }

    double getKurtosis(String s){
        Kurtosis k = new Kurtosis();
        double[] data = new double[size];
        if(s.equals("x")){
            for(int i=0; i<size-1; i++){
                data[i] = dataX.get(i);
            }
        } else if(s.equals("y")){
            for(int i=0; i<size-1; i++){
                data[i] = dataY.get(i);
            }
        } else{
            for(int i=0; i<size-1; i++){
                data[i] = dataZ.get(i);
            }
        }
        double res = k.evaluate(data);
        return String.valueOf(res).equals("NaN") ? 0 : res;
    }

    public double getQuartile(String s, double lowerPercent) {
        double[] values = new double[size];
        if(s.equals("x")){
            for(int i=0; i<size-1; i++){
                values[i] = dataX.get(i);
            }
        } else if(s.equals("y")){
            for(int i=0; i<size-1; i++){
                values[i] = dataY.get(i);
            }
        } else{
            for(int i=0; i<size-1; i++){
                values[i] = dataZ.get(i);
            }
        }


        if (values == null || size == 0) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }

        // Rank order the values
        double[] v = new double[size];
        System.arraycopy(values, 0, v, 0, size);
        Arrays.sort(v);

        int n = (int) Math.round(v.length * lowerPercent / 100);

        if(n == size)
            n --;

        return String.valueOf(v[n]).equals("NaN") ? 0 : v[n];

    }
}
