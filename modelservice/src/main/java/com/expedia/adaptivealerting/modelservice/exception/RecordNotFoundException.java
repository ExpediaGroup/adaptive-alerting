package com.expedia.adaptivealerting.modelservice.exception;

import lombok.Generated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Generated
@ResponseStatus(HttpStatus.NOT_FOUND)
public final class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(final String message) {
        super(message);
    }

}