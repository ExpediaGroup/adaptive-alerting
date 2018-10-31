package com.expedia.adaptivealerting.modelservice.web;

import com.expedia.adaptivealerting.modelservice.repo.ItemExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ItemExistsException.class)
    protected ResponseEntity<Object> handleConflict(ItemExistsException ex, WebRequest request) {

        // Not ideal but we'll handle existing items with a 200 response.
        // 303 (see other) might be reasonable but most clients will automatically
        // request the returned url which may not be desirable.
        return ResponseEntity.ok(ex.getExistingItem());
    }
}
