package com.ustmd.labwork3.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ustmd.labwork3.util.DateTimeUtil;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"uid", "operationUid", "systemTime", "code", "annualBonus", "quarterlyBonus",
        "errorCode", "errorMessage"})
public class Response {

    /**
     * Уникальный идентификатор сообщения
     */
    private String uid;

    /**
     * Уникальный идентификатор операции
     */
    private String operationUid;

    /**
     * Время создания ответа
     */
    private String systemTime;

    /**
     * Код результата обработки: success или failed
     */
    private Codes code;

    /**
     * Годовая премия сотрудника
     */
    private Double annualBonus;

    /**
     * Квартальная премия (только для управленцев)
     */
    private Double quarterlyBonus;

    /**
     * Код ошибки
     */
    private ErrorCodes errorCode;

    /**
     * Сообщение об ошибке
     */
    private ErrorMessages errorMessage;

    public static Response failure(Request request, ErrorCodes errorCode, ErrorMessages errorMessage) {
        return Response.builder()
                .uid(request == null || request.getUid() == null ? "" : request.getUid())
                .operationUid(request == null || request.getOperationUid() == null ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
