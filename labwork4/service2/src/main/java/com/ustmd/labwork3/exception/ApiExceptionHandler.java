package com.ustmd.labwork3.exception;

import com.ustmd.labwork3.model.ErrorCodes;
import com.ustmd.labwork3.model.ErrorMessages;
import com.ustmd.labwork3.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> handleInvalidJson(HttpMessageNotReadableException exception) {
        log.error("Ошибка чтения request: {}", exception.getMessage());
        Response response = Response.failure(null, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
        log.info("Создан и отправлен response после ошибки чтения request, HTTP 400: {}", response);
        return ResponseEntity.badRequest().body(response);
    }
}
