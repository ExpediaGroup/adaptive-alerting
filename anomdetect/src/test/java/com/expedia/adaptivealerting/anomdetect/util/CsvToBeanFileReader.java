package com.expedia.adaptivealerting.anomdetect.util;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CsvToBeanFileReader {
    // TODO HW: Reuse this in other tests
    public static <T> List<T> readData(String filename, Class<T> clazz) {
        final InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        return new CsvToBeanBuilder<T>(new InputStreamReader(is))
                .withType(clazz)
                .build()
                .parse();
    }
}
