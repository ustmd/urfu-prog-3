package com.ustmd.labwork3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

final class FeedbackTestSupport {

    private static final Set<String> RESPONSE_FIELDS = Set.of(
            "uid", "operationUid", "systemTime", "code", "errorCode", "errorMessage");

    private FeedbackTestSupport() {
    }

    static Map<String, Object> responseBody(ObjectMapper objectMapper, MvcResult result)
            throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<>() { });
    }

    static void assertResponseShape(Map<String, Object> response) {
        assertThat(response.keySet()).containsExactlyInAnyOrderElementsOf(RESPONSE_FIELDS);
        assertThat(response.values()).allSatisfy(value -> assertThat(value).isInstanceOf(String.class));
    }

    static void assertFailure(ObjectMapper objectMapper, MvcResult result,
                              String errorCode, String errorMessage) throws Exception {
        Map<String, Object> response = responseBody(objectMapper, result);
        assertResponseShape(response);
        assertThat(response).containsEntry("code", "failed")
                .containsEntry("errorCode", errorCode)
                .containsEntry("errorMessage", errorMessage);
        assertServerTime(response, Instant.now().minusSeconds(60));
    }

    static void assertServerTime(Map<String, Object> response, Instant beforeRequest) {
        assertThat(response.get("systemTime")).isInstanceOf(String.class);
        String systemTime = (String) response.get("systemTime");
        assertThat(systemTime).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        assertThat(Instant.parse(systemTime))
                .isBetween(beforeRequest.minusSeconds(1), Instant.now().plusSeconds(1));
    }

    static Map<String, Object> validRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("uid", "request-1");
        request.put("operationUid", "operation-1");
        request.put("systemTime", "2024-01-01T12:00:00Z");
        request.put("communicationId", 500);
        return request;
    }
}
