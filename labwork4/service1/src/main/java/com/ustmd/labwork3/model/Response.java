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
@JsonPropertyOrder({"uid", "operationUid", "systemTime", "code", "errorCode", "errorMessage"})
public class Response {

    private String uid;
    private String operationUid;
    private String systemTime;
    private Codes code;
    private ErrorCodes errorCode;
    private ErrorMessages errorMessage;

    public static Response failure(Request request, ErrorCodes errorCode, ErrorMessages errorMessage) {
        return new Response(
                request == null || request.getUid() == null ? "" : request.getUid(),
                request == null || request.getOperationUid() == null ? "" : request.getOperationUid(),
                DateTimeUtil.getCustomFormat().format(new Date()),
                Codes.FAILED, errorCode, errorMessage);
    }
}
