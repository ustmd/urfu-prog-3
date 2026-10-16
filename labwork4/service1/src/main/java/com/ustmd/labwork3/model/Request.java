package com.ustmd.labwork3.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Request {

    @NotBlank(message = "uid не должен быть пустым")
    @Size(max = 32, message = "uid должен содержать не более 32 символов")
    private String uid;

    @NotBlank(message = "operationUid не должен быть пустым")
    @Size(max = 32, message = "operationUid должен содержать не более 32 символов")
    private String operationUid;

    private Systems systemName;

    @NotBlank(message = "systemTime не должен быть пустым")
    private String systemTime;

    private String source;

    @NotNull(message = "communicationId обязателен")
    @Min(value = 1, message = "communicationId должен быть не меньше 1")
    @Max(value = 100000, message = "communicationId должен быть не больше 100000")
    private Integer communicationId;

    private Integer templateId;
    private Integer productCode;
    private Integer smsCode;

    @Override
    public String toString() {
        return "Request{uid='" + uid + "', operationUid='" + operationUid
                + "', systemName=" + systemName + ", systemTime='" + systemTime
                + "', source='" + source + "', communicationId=" + communicationId
                + ", templateId=" + templateId + ", productCode=" + productCode
                + ", smsCode=" + smsCode + "}";
    }
}
