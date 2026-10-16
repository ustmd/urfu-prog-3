package com.ustmd.labwork3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ustmd.labwork3.service.ValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;

import static com.ustmd.labwork3.FeedbackTestSupport.assertFailure;
import static com.ustmd.labwork3.FeedbackTestSupport.responseBody;
import static com.ustmd.labwork3.FeedbackTestSupport.validRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class UnexpectedFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ValidationService validationService;

    @Test
    void unexpectedServiceFailureProducesSafeServerErrorAndErrorLog(CapturedOutput output)
            throws Exception {
        String internalDetail = "internal detail that must not reach the client";
        doThrow(new IllegalStateException(internalDetail))
                .when(validationService).isValid(any(BindingResult.class));

        MvcResult result = mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(validRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        assertFailure(objectMapper, result, "UnknownException", "Произошла непредвиденная ошибка");
        assertThat(responseBody(objectMapper, result)).containsEntry("uid", "request-1")
                .containsEntry("operationUid", "operation-1");
        assertThat(result.getResponse().getContentAsString()).doesNotContain(internalDetail,
                "IllegalStateException", "stackTrace");
        assertThat(output.getAll()).contains("ERROR", internalDetail);
    }
}
