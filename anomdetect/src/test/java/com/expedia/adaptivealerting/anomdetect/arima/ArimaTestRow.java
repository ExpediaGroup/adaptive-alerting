package com.expedia.adaptivealerting.anomdetect.arima;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.ToString;

/**
 * The type Individual chart test row.
 *
 * @author ddivakar
 */
@Data
@ToString
public class ArimaTestRow {

    @CsvBindByName
    private int sample;

    @CsvBindByName
    private double observed;

    @CsvBindByName
    private double mu;

    @CsvBindByName
    private double forecast;

    @CsvBindByName
    private String anomalyLevel;

    @CsvBindByName
    private double predictedstatsmodelstsaarimamodel;
}
