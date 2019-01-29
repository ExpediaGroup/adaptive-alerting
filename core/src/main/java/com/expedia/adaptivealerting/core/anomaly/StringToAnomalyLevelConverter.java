package com.expedia.adaptivealerting.core.anomaly;

import com.opencsv.bean.AbstractBeanField;

/**
 * Used to deserialize an AnomalyLevel object from a CSV file.
 * Used with @CsvCustomBindByName within a CsvToBeanBuilder Reader.
 *
 * <p>
 * E.g.
 * <pre>     @CsvCustomBindByName(converter = StringToAnomalyLevelConverter.class)
 *   private AnomalyLevel expectedLevel;
 * </pre>
 */
public class StringToAnomalyLevelConverter extends AbstractBeanField {
    @Override
    protected Object convert(String value) {
        return AnomalyLevel.valueOf(value);
    }
}
