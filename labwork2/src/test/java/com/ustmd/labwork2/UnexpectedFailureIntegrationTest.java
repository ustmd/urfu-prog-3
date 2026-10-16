package com.ustmd.labwork2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ustmd.labwork2.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UnexpectedFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ValidationService validationService;

    @Test
    void unexpectedServiceFailureProducesSafeStructuredServerError() throws Exception {
        doThrow(new IllegalStateException("internal detail that must not reach the client"))
                .when(validationService).isValid(any(BindingResult.class));
        Instant beforeRequest = Instant.now();

        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "uid": "request-1",
                                  "operationUid": "operation-1",
                                  "systemTime": "2024-01-01T12:00:00Z",
                                  "communicationId": 1
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        Map<String, Object> response = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), new TypeReference<>() { });
        assertThat(response.keySet()).containsExactlyInAnyOrder(
                "uid", "operationUid", "systemTime", "code", "errorCode", "errorMessage");
        assertThat(response).containsEntry("code", "failed")
                .containsEntry("errorCode", "UnknownException")
                .containsEntry("errorMessage", "Произошла непредвиденная ошибка");
        assertThat(response.get("systemTime")).isInstanceOf(String.class);
        assertThat((String) response.get("systemTime")).endsWith("Z");
        assertThat(Instant.parse((String) response.get("systemTime")))
                .isBetween(beforeRequest.minusSeconds(1), Instant.now().plusSeconds(1));
    }
}
