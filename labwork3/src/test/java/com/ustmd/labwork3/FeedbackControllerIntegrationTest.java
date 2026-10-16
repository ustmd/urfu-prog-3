package com.ustmd.labwork3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ustmd.labwork3.model.Request;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ustmd.labwork3.FeedbackTestSupport.assertFailure;
import static com.ustmd.labwork3.FeedbackTestSupport.assertResponseShape;
import static com.ustmd.labwork3.FeedbackTestSupport.assertServerTime;
import static com.ustmd.labwork3.FeedbackTestSupport.responseBody;
import static com.ustmd.labwork3.FeedbackTestSupport.validRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Validator validator;

    @Test
    void successfulRequestUsesTimeModifierAndPreservesOperationUid(CapturedOutput output)
            throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemTime", "2000-01-01T00:00:00Z");
        Instant beforeRequest = Instant.now();

        Map<String, Object> response = responseBody(objectMapper, postJson(request, 200));

        assertResponseShape(response);
        assertThat(response).containsEntry("uid", request.get("uid"))
                .containsEntry("operationUid", request.get("operationUid"))
                .containsEntry("code", "success")
                .containsEntry("errorCode", "")
                .containsEntry("errorMessage", "");
        assertServerTime(response, beforeRequest);
        assertThat(response.get("systemTime")).isNotEqualTo(request.get("systemTime"));
        assertThat(output.getAll()).contains("INFO", "request-1", "2000-01-01T00:00:00Z",
                (String) response.get("systemTime"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100000})
    void communicationIdBoundsAndMaximumIdentifierLengthsAreAccepted(int communicationId)
            throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "u".repeat(32));
        request.put("operationUid", "o".repeat(32));
        request.put("communicationId", communicationId);

        Map<String, Object> response = responseBody(objectMapper, postJson(request, 200));

        assertThat(response).containsEntry("code", "success")
                .containsEntry("uid", request.get("uid"))
                .containsEntry("operationUid", request.get("operationUid"));
    }

    @Test
    void optionalFieldsMayBeOmittedAndSystemTimeNeedOnlyBeNonblank() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemTime", "not-an-ISO-timestamp");

        Map<String, Object> response = responseBody(objectMapper, postJson(request, 200));

        assertThat(response).containsEntry("code", "success")
                .containsEntry("errorCode", "")
                .containsEntry("errorMessage", "");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ERP", "CRM", "WMS"})
    void supportedSystemsAndAllOptionalFieldsAreAccepted(String systemName) throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemName", systemName);
        request.put("source", "laboratory");
        request.put("templateId", 5);
        request.put("productCode", 27);
        request.put("smsCode", 1234);

        Map<String, Object> response = responseBody(objectMapper, postJson(request, 200));

        assertResponseShape(response);
        assertThat(response).containsEntry("code", "success");
    }

    @Test
    void optionalFieldsMayBeExplicitlyNull() throws Exception {
        Map<String, Object> request = validRequest();
        for (String field : new String[]{"systemName", "source", "templateId", "productCode", "smsCode"}) {
            request.put(field, null);
        }

        assertThat(responseBody(objectMapper, postJson(request, 200)))
                .containsEntry("code", "success");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSystems")
    void invalidSystemNamesProduceValidationErrorWithEmptyIdentifiers(String description,
                                                                     Object systemName)
            throws Exception {
        Map<String, Object> request = validRequest();
        request.put("systemName", systemName);

        MvcResult result = postJson(request, 400);

        assertFailure(objectMapper, result, "ValidationException", "Ошибка валидации");
        assertThat(responseBody(objectMapper, result)).containsEntry("uid", "")
                .containsEntry("operationUid", "");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    void invalidFieldsProduceValidationError(String description, Map<String, Object> request)
            throws Exception {
        assertFailure(objectMapper, postJson(request, 400),
                "ValidationException", "Ошибка валидации");
    }

    @Test
    void everyValidationMessageIsLoggedAtErrorLevel(CapturedOutput output) throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", null);
        request.put("operationUid", "o".repeat(33));
        request.put("systemTime", "");
        request.put("communicationId", 100001);
        Map<String, Long> expectedMessages = validator
                .validate(objectMapper.convertValue(request, Request.class)).stream()
                .collect(Collectors.groupingBy(violation -> violation.getMessage(), Collectors.counting()));

        assertFailure(objectMapper, postJson(request, 400),
                "ValidationException", "Ошибка валидации");

        assertThat(expectedMessages).isNotEmpty();
        var errorLines = output.getAll().lines().filter(line -> line.contains("ERROR")).toList();
        expectedMessages.forEach((message, count) ->
                assertThat(errorLines.stream().filter(line -> line.contains(message)).count())
                        .as("ERROR log entries for validation message: %s", message)
                        .isGreaterThanOrEqualTo(count));
    }

    @Test
    void uid123ProducesUnsupportedCodeErrorAndErrorLog(CapturedOutput output) throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "123");

        MvcResult result = postJson(request, 400);

        assertFailure(objectMapper, result, "UnsupportedCodeException", "Не поддерживаемая ошибка");
        assertThat(responseBody(objectMapper, result)).containsEntry("uid", "123")
                .containsEntry("operationUid", request.get("operationUid"));
        assertThat(output.getAll()).contains("ERROR", "UnsupportedCodeException");
    }

    @Test
    void validationHappensBeforeUnsupportedCodeCheck() throws Exception {
        Map<String, Object> request = validRequest();
        request.put("uid", "123");
        request.put("operationUid", "");

        assertFailure(objectMapper, postJson(request, 400),
                "ValidationException", "Ошибка валидации");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "null", "[]", "{\"communicationId\":\"not-a-number\"}",
            "{\"systemName\":\"UNKNOWN\"}"})
    void unreadableBodiesProduceValidationErrorWithEmptyIdentifiers(String body,
                                                                   CapturedOutput output)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertFailure(objectMapper, result, "ValidationException", "Ошибка валидации");
        assertThat(responseBody(objectMapper, result)).containsEntry("uid", "")
                .containsEntry("operationUid", "");
        assertThat(output.getAll()).contains("ERROR");
    }

    @Test
    void missingBodyProducesValidationErrorWithEmptyIdentifiers() throws Exception {
        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertFailure(objectMapper, result, "ValidationException", "Ошибка валидации");
        assertThat(responseBody(objectMapper, result)).containsEntry("uid", "")
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
        for (Object communicationId : new Object[]{null, -1, 0, 100001}) {
            Map<String, Object> request = validRequest();
            request.put("communicationId", communicationId);
            requests.add(Arguments.of("communicationId " + communicationId, request));
        }
        Map<String, Object> missingCommunicationId = validRequest();
        missingCommunicationId.remove("communicationId");
        requests.add(Arguments.of("communicationId missing", missingCommunicationId));
        return requests.build();
    }

    private static Stream<Arguments> invalidSystems() {
        return Stream.of(
                Arguments.of("unknown system", "UNKNOWN"),
                Arguments.of("lowercase system", "erp"),
                Arguments.of("empty system", ""),
                Arguments.of("numeric enum ordinal", 1));
    }
}
