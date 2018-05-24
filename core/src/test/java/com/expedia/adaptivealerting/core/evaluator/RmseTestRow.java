package com.expedia.adaptivealerting.core.evaluator;

import com.opencsv.bean.CsvBindByName;

public class RmseTestRow {

    @CsvBindByName
    private int observed;

    @CsvBindByName
    private double predicted;

    @CsvBindByName
    private double rmse;

    public int getObserved() {
        return observed;
    }

    public void setObserved(int observed) {
        this.observed = observed;
    }

    public double getPredicted() {
        return predicted;
    }

    public void setPredicted(double predicted) {
        this.predicted = predicted;
    }

    public double getRmse() {
        return rmse;
    }

    public void setKnownMean(double rmse) {
        this.rmse = rmse;
    }
}
