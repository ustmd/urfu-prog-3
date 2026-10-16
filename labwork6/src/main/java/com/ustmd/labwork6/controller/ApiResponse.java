package com.ustmd.labwork6.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

// Ответ об успехе или неуспехе операции
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

}
