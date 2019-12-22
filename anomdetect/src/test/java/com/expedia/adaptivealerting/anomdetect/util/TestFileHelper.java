package com.expedia.adaptivealerting.anomdetect.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import static java.net.URLDecoder.decode;
import static java.util.Objects.requireNonNull;

public class TestFileHelper {
    public static File getResourceAsFile(String name) {
        try {
            URL url = ClassLoader.getSystemResource(name);
            // We use decode() to convert escaped path elements to localised format (e.g. "%20" --> " ")
            String filePath = decode(requireNonNull(url).getFile(), "UTF-8");
            return new File(filePath);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
