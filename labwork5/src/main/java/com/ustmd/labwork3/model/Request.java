package com.ustmd.labwork3.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    /**
     * Уникальный идентификатор сообщения
     */
    @NotBlank(message = "uid не должен быть пустым")
    @Size(max = 32, message = "uid должен содержать не более 32 символов")
    private String uid;

    /**
     * Уникальный идентификатор операции
     */
    @NotBlank(message = "operationUid не должен быть пустым")
    @Size(max = 32, message = "operationUid должен содержать не более 32 символов")
    private String operationUid;

    /**
     * Имя системы отправителя
     */
    private Systems systemName;

    /**
     * Время создания сообщения
     */
    @NotBlank(message = "systemTime не должен быть пустым")
    private String systemTime;

    /**
     * Наименование ресурса
     */
    private String source;

    /**
     * Должность сотрудника
     */
    private Positions position;

    /**
     * Заработная плата сотрудника
     */
    private Double salary;

    /**
     * Коэффициент премии
     */
    private Double bonus;

    /**
     * Количество отработанных дней
     */
    private Integer workDays;

    /**
     * Уникальный идентификатор коммуникации
     */
    @NotNull(message = "communicationId обязателен")
    @Min(value = 1, message = "communicationId должен быть не меньше 1")
    @Max(value = 100000, message = "communicationId должен быть не больше 100000")
    private Integer communicationId;

    /**
     * Уникальный идентификатор шаблона
     */
    private Integer templateId;

    /**
     * Код продукта
     */
    private Integer productCode;

    /**
     * Смс код
     */
    private Integer smsCode;

    @Override
    public String toString() {
        return "Request{uid='" + uid + "', operationUid='" + operationUid
                + "', systemName=" + systemName + ", systemTime='" + systemTime
                + "', source='" + source + "', position=" + position
                + ", salary=" + salary + ", bonus=" + bonus + ", workDays=" + workDays
                + ", communicationId=" + communicationId
                + ", templateId=" + templateId + ", productCode=" + productCode
                + ", smsCode=" + smsCode + "}";
    }
}
