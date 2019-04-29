package com.expedia.adaptivealerting.anomdetect.detectormapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    private String key;
    private String value;
}
