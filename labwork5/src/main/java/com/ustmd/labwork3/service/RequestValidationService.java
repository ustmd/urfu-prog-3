package com.ustmd.labwork3.service;

import com.ustmd.labwork3.exception.ValidationFailedException;
import com.ustmd.labwork3.model.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Slf4j
@Service
public class RequestValidationService implements ValidationService {

    @Override
    public void isValid(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                String field = error instanceof FieldError fieldError
                        ? fieldError.getField() : error.getObjectName();
                log.error("Ошибка валидации поля {}: {}", field, error.getDefaultMessage());
            });
            log.error("ValidationFailedException: {}", ErrorMessages.VALIDATION);
            throw new ValidationFailedException(ErrorMessages.VALIDATION.getName());
        }
        log.info("Валидация request завершена успешно");
    }
}
