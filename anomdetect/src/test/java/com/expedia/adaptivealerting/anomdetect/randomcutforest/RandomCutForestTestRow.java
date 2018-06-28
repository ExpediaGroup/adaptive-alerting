package com.expedia.adaptivealerting.anomdetect.randomcutforest;

import com.opencsv.bean.CsvBindByName;

public class RandomCutForestTestRow {
    @CsvBindByName
    private double observed;

    public double getObserved() {
        return observed;
    }

    public void setObserved(double observed) {
        this.observed = observed;
    }
}
