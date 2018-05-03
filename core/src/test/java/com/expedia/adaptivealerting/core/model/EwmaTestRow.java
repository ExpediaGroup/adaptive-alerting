package com.expedia.adaptivealerting.core.model;

import com.opencsv.bean.CsvBindByName;

public class EwmaTestRow {
    
    @CsvBindByName
    private String date;
    
    @CsvBindByName
    private int observed;
    
    @CsvBindByName
    private double mean;
    
    @CsvBindByName(column = "known.mean")
    private double knownMean;
    
    @CsvBindByName
    private double var;
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public int getObserved() {
        return observed;
    }
    
    public void setObserved(int observed) {
        this.observed = observed;
    }
    
    public double getMean() {
        return mean;
    }
    
    public void setMean(double mean) {
        this.mean = mean;
    }
    
    public double getKnownMean() {
        return knownMean;
    }
    
    public void setKnownMean(double knownMean) {
        this.knownMean = knownMean;
    }
    
    public double getVar() {
        return var;
    }
    
    public void setVar(double var) {
        this.var = var;
    }
}
