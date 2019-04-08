package com.expedia.adaptivealerting.modelservice.detectormapper.util;


import com.expedia.adaptivealerting.modelservice.detectormapper.service.Detector;
import com.expedia.adaptivealerting.modelservice.detectormapper.service.DetectorMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DetectorUtil {
    
    //FIXME - Now we are using '=' as delimiter betwen tag name and value.
    // If any tag name or value has a '=' in it, the {@link #getTags(String) getTags} method wont work as expected.
    public static final String DELIMITER = "=";
    
    private DetectorUtil() {}
    
    public static String getKey(Map<String,String> tags) {
        List<String> listOfEntries = tags.entrySet()
            .stream()
            .map(entry -> entry.getKey() + DELIMITER + entry.getValue())
            .collect(Collectors.toList());
        Collections.sort(listOfEntries);
        return String.join(",", listOfEntries);
    }

    public static Map<String,String> getTags(String hashKey) {
        String[] keyVals = hashKey.split(",");
        Map<String, String> tags = new HashMap<>();
        Arrays.asList(keyVals).forEach(keyVal -> {
            String[] kv = keyVal.split(DELIMITER);
            tags.put(kv[0], kv[1]);
        });
        return tags;
    }

    public static String getDetectorIdsString(List<Detector> list) {
        List<String> result = new ArrayList<>();
        list.forEach(detector -> {
            result.add(detector.getId().toString());
        });
        return String.join("|", result);
    }

    public static List<Detector> buildDetectors(String detectorIdsString) {
        if (detectorIdsString == null || "".equals(detectorIdsString)) {
            return Collections.emptyList();
        }
        String[] detectorList = detectorIdsString.split("\\|");
        return Arrays.asList(detectorList).stream().map(dt ->  new Detector(UUID.fromString(dt))).collect(Collectors.toList());
    }

    public static List<UUID> getDetectorIds(List<DetectorMapping> detectorMappings) {
        return detectorMappings
                .stream().map(detectorMapping -> detectorMapping.getDetector().getId())
                .collect(Collectors.toList());
    }


}
