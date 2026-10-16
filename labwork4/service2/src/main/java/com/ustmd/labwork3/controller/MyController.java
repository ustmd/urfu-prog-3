package com.ustmd.labwork3.controller;

import com.ustmd.labwork3.exception.UnsupportedCodeException;
import com.ustmd.labwork3.exception.ValidationFailedException;
import com.ustmd.labwork3.model.Codes;
import com.ustmd.labwork3.model.ErrorCodes;
import com.ustmd.labwork3.model.ErrorMessages;
import com.ustmd.labwork3.model.Request;
import com.ustmd.labwork3.model.Response;
import com.ustmd.labwork3.service.ModifyResponseService;
import com.ustmd.labwork3.service.ValidationService;
import com.ustmd.labwork3.util.DateTimeUtil;
import jakarta.validation.Valid;
import java.text.ParseException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MyController {

    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;

    public MyController(ValidationService validationService,
            @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
    }

    @PostMapping(value = "/feedback", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
            BindingResult bindingResult) {
        long receivedTime = System.currentTimeMillis();
        log.info("Получен request: {}", request);
        logElapsedTime(request, receivedTime);
        Response response = Response.builder()
                .uid(request.getUid() == null ? "" : request.getUid())
                .operationUid(request.getOperationUid() == null ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();
        log.info("uid={}: создан response: {}", request.getUid(), response);

        HttpStatus status = HttpStatus.OK;
        try {
            validationService.isValid(bindingResult);
            if ("123".equals(request.getUid())) {
                log.error("uid={}: UnsupportedCodeException: {}", request.getUid(), ErrorMessages.UNSUPPORTED);
                throw new UnsupportedCodeException(ErrorMessages.UNSUPPORTED.getName());
            }
            response = modifyResponseService.modify(response);
        } catch (ValidationFailedException exception) {
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.VALIDATION_EXCEPTION);
            response.setErrorMessage(ErrorMessages.VALIDATION);
            status = HttpStatus.BAD_REQUEST;
            log.info("uid={}: response изменен после ошибки валидации: {}", request.getUid(), response);
        } catch (UnsupportedCodeException exception) {
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNSUPPORTED_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNSUPPORTED);
            status = HttpStatus.BAD_REQUEST;
            log.info("uid={}: response изменен после UnsupportedCodeException: {}", request.getUid(), response);
        } catch (Exception exception) {
            log.error("uid={}: непредвиденная ошибка обработки request", request.getUid(), exception);
            response.setCode(Codes.FAILED);
            response.setErrorCode(ErrorCodes.UNKNOWN_EXCEPTION);
            response.setErrorMessage(ErrorMessages.UNKNOWN);
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.info("uid={}: response изменен после непредвиденной ошибки: {}", request.getUid(), response);
        }

        log.info("uid={}: отправлен response, HTTP {}: {}", request.getUid(), status.value(), response);
        return ResponseEntity.status(status).body(response);
    }

    private void logElapsedTime(Request request, long receivedTime) {
        try {
            long sendTime = DateTimeUtil.getCustomFormat().parse(request.getSystemTime()).getTime();
            log.info("uid={}: время от отправки request до получения Сервисом 2: {} мс",
                    request.getUid(), receivedTime - sendTime);
        } catch (ParseException | NullPointerException exception) {
            log.warn("uid={}: время не вычислено, некорректный systemTime: {}",
                    request.getUid(), request.getSystemTime());
        }
    }
}
