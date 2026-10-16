package com.ustmd.labwork2.service;

import com.ustmd.labwork2.exception.ValidationFailedException;
import org.springframework.validation.BindingResult;

public interface ValidationService {

    void isValid(BindingResult bindingResult) throws ValidationFailedException;
}
