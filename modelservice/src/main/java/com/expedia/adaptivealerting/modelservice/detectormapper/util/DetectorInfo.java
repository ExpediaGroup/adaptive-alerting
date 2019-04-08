package com.expedia.adaptivealerting.modelservice.detectormapper.util;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class DetectorInfo {
    
    private Long id;
    private String uuid;
    private String createdBy;
    private Timestamp lastUpdateTimestamp;
    private Boolean enabled;

}
