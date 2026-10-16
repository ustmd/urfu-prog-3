package com.ustmd.labwork2.controller;

import com.ustmd.labwork2.exception.UnsupportedCodeException;
import com.ustmd.labwork2.exception.ValidationFailedException;
import com.ustmd.labwork2.model.Request;
import com.ustmd.labwork2.model.Response;
import com.ustmd.labwork2.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyController {

    private final ValidationService validationService;

    @PostMapping(value = "/feedback", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
            BindingResult bindingResult) {
        try {
            validationService.isValid(bindingResult);
            if ("123".equals(request.getUid())) {
                throw new UnsupportedCodeException("Не поддерживаемая ошибка");
            }
            return ResponseEntity.ok(Response.success(request));
        } catch (ValidationFailedException exception) {
            return ResponseEntity.badRequest().body(Response.failure(
                    request, "ValidationException", exception.getMessage()));
        } catch (UnsupportedCodeException exception) {
            return ResponseEntity.badRequest().body(Response.failure(
                    request, "UnsupportedCodeException", exception.getMessage()));
        } catch (Exception exception) {
            log.error("Непредвиденная ошибка обработки запроса", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Response.failure(
                    request, "UnknownException", "Произошла непредвиденная ошибка"));
        }
    }
}
