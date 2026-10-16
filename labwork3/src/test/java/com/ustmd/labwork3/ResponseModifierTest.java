package com.ustmd.labwork3;

import com.ustmd.labwork3.model.Codes;
import com.ustmd.labwork3.model.ErrorCodes;
import com.ustmd.labwork3.model.ErrorMessages;
import com.ustmd.labwork3.model.Response;
import com.ustmd.labwork3.service.ModifyOperationUidResponseService;
import com.ustmd.labwork3.service.ModifySystemTimeResponseService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseModifierTest {

    @Test
    void systemTimeModifierReturnsSameResponseAndChangesOnlySystemTime() {
        Response response = response();
        Instant beforeModification = Instant.now();

        Response result = new ModifySystemTimeResponseService().modify(response);

        assertThat(result).isSameAs(response);
        assertThat(result.getSystemTime())
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        assertThat(Instant.parse(result.getSystemTime()))
                .isBetween(beforeModification.minusSeconds(1), Instant.now().plusSeconds(1));
        assertThat(result.getUid()).isEqualTo("request-1");
        assertThat(result.getOperationUid()).isEqualTo("operation-1");
        assertThat(result.getCode()).isEqualTo(Codes.FAILED);
        assertThat(result.getErrorCode()).isEqualTo(ErrorCodes.VALIDATION_EXCEPTION);
        assertThat(result.getErrorMessage()).isEqualTo(ErrorMessages.VALIDATION);
    }

    @Test
    void operationUidModifierReturnsSameResponseAndChangesOnlyOperationUid() {
        Response response = response();

        Response result = new ModifyOperationUidResponseService().modify(response);

        assertThat(result).isSameAs(response);
        assertThat(result.getOperationUid()).isNotEqualTo("operation-1");
        assertThat(UUID.fromString(result.getOperationUid()).version()).isEqualTo(4);
        assertThat(result.getUid()).isEqualTo("request-1");
        assertThat(result.getSystemTime()).isEqualTo("2000-01-01T00:00:00.000Z");
        assertThat(result.getCode()).isEqualTo(Codes.FAILED);
        assertThat(result.getErrorCode()).isEqualTo(ErrorCodes.VALIDATION_EXCEPTION);
        assertThat(result.getErrorMessage()).isEqualTo(ErrorMessages.VALIDATION);
    }

    @Test
    void operationUidModifierGeneratesNewUuidOnEachInvocation() {
        Response response = response();
        ModifyOperationUidResponseService service = new ModifyOperationUidResponseService();
        service.modify(response);
        String firstOperationUid = response.getOperationUid();

        service.modify(response);

        assertThat(response.getOperationUid()).isNotEqualTo(firstOperationUid);
        assertThat(UUID.fromString(response.getOperationUid()).version()).isEqualTo(4);
    }

    private static Response response() {
        return new Response("request-1", "operation-1", "2000-01-01T00:00:00.000Z",
                Codes.FAILED, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
    }
}
