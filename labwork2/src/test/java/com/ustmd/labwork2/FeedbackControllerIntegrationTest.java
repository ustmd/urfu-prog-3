package com.ustmd.labwork2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeedbackControllerIntegrationTest {

    private static final Set<String> RESPONSE_FIELDS = Set.of(
            "uid", "operationUid", "systemTime", "code", "errorCode", "errorMessage");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successfulRequestReturnsOnlyResponseFieldsAndServerTime() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemTime", "2000-01-01T00:00:00Z");
        Instant beforeRequest = Instant.now();

        MvcResult result = postJson(request, 200);

        Map<String, Object> response = responseBody(result);
        assertThat(response.keySet()).containsExactlyInAnyOrderElementsOf(RESPONSE_FIELDS);
        assertThat(response).containsEntry("uid", request.get("uid"))
                .containsEntry("operationUid", request.get("operationUid"))
                .containsEntry("code", "success")
                .containsEntry("errorCode", "")
                .containsEntry("errorMessage", "");
        assertServerTime(response, beforeRequest);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100000})
    void communicationIdBoundsAndMaximumIdentifierLengthsAreAccepted(int communicationId)
            throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "u".repeat(32));
        request.put("operationUid", "o".repeat(32));
        request.put("communicationId", communicationId);

        Map<String, Object> response = responseBody(postJson(request, 200));

        assertThat(response).containsEntry("code", "success")
                .containsEntry("uid", request.get("uid"))
                .containsEntry("operationUid", request.get("operationUid"));
    }

    @Test
    void optionalFieldsMayBeOmittedAndSystemTimeNeedOnlyBeNonblank() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemTime", "not-an-ISO-timestamp");

        Map<String, Object> response = responseBody(postJson(request, 200));

        assertThat(response).containsEntry("code", "success")
                .containsEntry("errorCode", "")
                .containsEntry("errorMessage", "");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    void invalidFieldsProduceValidationError(String description, Map<String, Object> request)
            throws Exception {
        assertFailure(postJson(request, 400), "ValidationException", "Ошибка валидации");
    }

    @Test
    void severalInvalidFieldsProduceOneStructuredValidationResponse() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", " ");
        request.put("operationUid", "o".repeat(33));
        request.put("systemTime", "");
        request.put("communicationId", 100001);

        assertFailure(postJson(request, 400), "ValidationException", "Ошибка валидации");
    }

    @Test
    void uid123ProducesUnsupportedCodeError() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "123");

        MvcResult result = postJson(request, 400);

        assertFailure(result, "UnsupportedCodeException", "Не поддерживаемая ошибка");
        assertThat(responseBody(result)).containsEntry("uid", "123")
                .containsEntry("operationUid", request.get("operationUid"));
    }

    @Test
    void validationHappensBeforeUnsupportedCodeCheck() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "123");
        request.put("operationUid", "");

        assertFailure(postJson(request, 400), "ValidationException", "Ошибка валидации");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "[]", "{\"communicationId\":\"not-a-number\"}"})
    void unreadableRequestBodiesProduceStructuredValidationError(String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertFailure(result, "ValidationException", "Ошибка валидации");
        assertThat(responseBody(result)).containsEntry("uid", "")
                .containsEntry("operationUid", "");
    }

    @Test
    void missingRequestBodyProducesStructuredValidationError() throws Exception {
        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertFailure(result, "ValidationException", "Ошибка валидации");
        assertThat(responseBody(result)).containsEntry("uid", "")
                .containsEntry("operationUid", "");
    }

    private MvcResult postJson(Map<String, Object> request, int expectedStatus) throws Exception {
        return mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    private Map<String, Object> responseBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                new TypeReference<>() { });
    }

    private void assertFailure(MvcResult result, String errorCode, String errorMessage)
            throws Exception {
        Map<String, Object> response = responseBody(result);
        assertThat(response.keySet()).containsExactlyInAnyOrderElementsOf(RESPONSE_FIELDS);
        assertThat(response).containsEntry("code", "failed")
                .containsEntry("errorCode", errorCode)
                .containsEntry("errorMessage", errorMessage);
        assertThat(response.get("systemTime")).isInstanceOf(String.class);
        assertThat((String) response.get("systemTime")).endsWith("Z");
        assertThat(Instant.parse((String) response.get("systemTime")))
                .isBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(1));
    }

    private static void assertServerTime(Map<String, Object> response, Instant beforeRequest) {
        assertThat(response.get("systemTime")).isInstanceOf(String.class);
        assertThat((String) response.get("systemTime")).endsWith("Z");
        assertThat(Instant.parse((String) response.get("systemTime")))
                .isBetween(beforeRequest.minusSeconds(1), Instant.now().plusSeconds(1));
    }

    private static Map<String, Object> validRequest() {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("uid", "request-1");
        request.put("operationUid", "operation-1");
        request.put("systemTime", "2024-01-01T12:00:00Z");
        request.put("communicationId", 500);
        return request;
    }

    private static Stream<Arguments> invalidRequests() {
        Stream.Builder<Arguments> requests = Stream.builder();
        for (String field : new String[]{"uid", "operationUid", "systemTime"}) {
            Map<String, Object> missing = validRequest();
            missing.remove(field);
            requests.add(Arguments.of(field + " missing", missing));
            for (Object value : new Object[]{null, "", " ", "\t\n"}) {
                Map<String, Object> request = validRequest();
                request.put(field, value);
                requests.add(Arguments.of(field + " invalid: " + String.valueOf(value), request));
            }
        }
        for (String field : new String[]{"uid", "operationUid"}) {
            Map<String, Object> request = validRequest();
            request.put(field, "x".repeat(33));
            requests.add(Arguments.of(field + " longer than 32", request));
        }
        for (int communicationId : new int[]{-1, 0, 100001}) {
            Map<String, Object> request = validRequest();
            request.put("communicationId", communicationId);
            requests.add(Arguments.of("communicationId " + communicationId, request));
        }
        Map<String, Object> missingCommunicationId = validRequest();
        missingCommunicationId.remove("communicationId");
        requests.add(Arguments.of("communicationId missing", missingCommunicationId));
        return requests.build();
    }
}
