package com.alesp.feedbackapp;

import java.util.ArrayList;

/**
 * Created by alesp on 28/03/2017.
 */

public class WordEntity {

    //creo le variabili che identificano la mia parola
    private String word;
    private double eatingRelevance;
    private double breakfastRelevance;
    private double lunchRelevance;
    private double tkmedRelevance;
    private double stuptabRelevance;
    private double cleartabRelevance;

    public WordEntity(String word, double eatingRelevance, double lunchRelevance, double breakfastRelevance, double tkmedRelevance, double stuptabRelevance, double cleartabRelevance) {
        this.word = word;
        this.eatingRelevance = eatingRelevance;
        this.lunchRelevance = lunchRelevance;
        this.breakfastRelevance = breakfastRelevance;
        this.tkmedRelevance = tkmedRelevance;
        this.stuptabRelevance = stuptabRelevance;
        this.cleartabRelevance = cleartabRelevance;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getEatingRelevance() {
        return eatingRelevance;
    }

    public void setEatingRelevance(double eatingRelevance) {
        this.eatingRelevance = eatingRelevance;
    }

    public double getBreakfastRelevance() {
        return breakfastRelevance;
    }

    public void setBreakfastRelevance(double breakfastRelevance) {
        this.breakfastRelevance = breakfastRelevance;
    }

    public double getLunchRelevance() {
        return lunchRelevance;
    }

    public void setLunchRelevance(double lunchRelevance) {
        this.lunchRelevance = lunchRelevance;
    }

    public double getTkmedRelevance() {
        return tkmedRelevance;
    }

    public void setTkmedRelevance(double tkmedRelevance) {
        this.tkmedRelevance = tkmedRelevance;
    }

    public double getCleartabRelevance() {
        return cleartabRelevance;
    }

    public void setCleartabRelevance(double cleartabRelevance) {
        this.cleartabRelevance = cleartabRelevance;
    }

    public double getStuptabRelevance() {
        return stuptabRelevance;
    }

    public void setStuptabRelevance(double stuptabRelevance) {
        this.stuptabRelevance = stuptabRelevance;
    }

    public static ArrayList<Double> sumValues(WordEntity first, WordEntity second){
        ArrayList<Double> res = new ArrayList<Double>();


    }
}
