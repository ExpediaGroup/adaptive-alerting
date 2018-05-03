package com.expedia.adaptivealerting.core.model;

import com.opencsv.bean.CsvBindByName;

public class EwmaTestRow {
    
    @CsvBindByName
    private String date;
    
    @CsvBindByName
    private int observed;
    
    @CsvBindByName
    private double diff;
    
    @CsvBindByName
    private double incr;
    
    @CsvBindByName
    private double mean;
    
    @CsvBindByName
    private double var;
    
    @CsvBindByName
    private double stdev;
    
    @CsvBindByName(column = "warn_up")
    private double warnUp;
    
    @CsvBindByName(column = "warn_lo")
    private double warnLo;
    
    @CsvBindByName(column = "crit_up")
    private double critUp;
    
    @CsvBindByName(column = "crit_lo")
    private double critLo;
    
    @CsvBindByName
    private double dist;
    
    @CsvBindByName
    private double sigmas;
    
    @CsvBindByName
    private String level;
    
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
    
    public double getDiff() {
        return diff;
    }
    
    public void setDiff(double diff) {
        this.diff = diff;
    }
    
    public double getIncr() {
        return incr;
    }
    
    public void setIncr(double incr) {
        this.incr = incr;
    }
    
    public double getMean() {
        return mean;
    }
    
    public void setMean(double mean) {
        this.mean = mean;
    }
    
    public double getVar() {
        return var;
    }
    
    public void setVar(double var) {
        this.var = var;
    }
    
    public double getStdev() {
        return stdev;
    }
    
    public void setStdev(double stdev) {
        this.stdev = stdev;
    }
    
    public double getWarnUp() {
        return warnUp;
    }
    
    public void setWarnUp(double warnUp) {
        this.warnUp = warnUp;
    }
    
    public double getWarnLo() {
        return warnLo;
    }
    
    public void setWarnLo(double warnLo) {
        this.warnLo = warnLo;
    }
    
    public double getCritUp() {
        return critUp;
    }
    
    public void setCritUp(double critUp) {
        this.critUp = critUp;
    }
    
    public double getCritLo() {
        return critLo;
    }
    
    public void setCritLo(double critLo) {
        this.critLo = critLo;
    }
    
    public double getDist() {
        return dist;
    }
    
    public void setDist(double dist) {
        this.dist = dist;
    }
    
    public double getSigmas() {
        return sigmas;
    }
    
    public void setSigmas(double sigmas) {
        this.sigmas = sigmas;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
}
