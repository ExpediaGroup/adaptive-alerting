package com.expedia.adaptivealerting.anomdetect.randomcutforest.util;

import java.io.IOException;

public class FailedToLoadPropertiesException extends RuntimeException
{
    public FailedToLoadPropertiesException(IOException e) {
        super(e);
    }
}
