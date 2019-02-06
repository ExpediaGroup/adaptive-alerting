package com.expedia.adaptivealerting.modelservice.graphite;

import lombok.Data;

@Data
public class Tags {

    private String name;

    @Override
    public String toString() {
        return "ClassPojo [name = " + name + "]";
    }
}

