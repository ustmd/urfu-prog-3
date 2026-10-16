package com.ustmd.labwork2.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"uid", "operationUid", "systemTime", "code", "errorCode", "errorMessage"})
public class Response {

    private String uid;
    private String operationUid;
    private String systemTime;
    private String code;
    private String errorCode;
    private String errorMessage;

    public static Response success(Request request) {
        return create(request, "success", "", "");
    }

    public static Response failure(Request request, String errorCode, String errorMessage) {
        return create(request, "failed", errorCode, errorMessage);
    }

    private static Response create(Request request, String code, String errorCode, String errorMessage) {
        return new Response(
                request == null || request.getUid() == null ? "" : request.getUid(),
                request == null || request.getOperationUid() == null ? "" : request.getOperationUid(),
                Instant.now().toString(), code, errorCode, errorMessage);
    }
}
