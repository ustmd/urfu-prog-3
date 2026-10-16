package com.ustmd.labwork3.controller;

import com.ustmd.labwork3.exception.UnsupportedCodeException;
import com.ustmd.labwork3.exception.ValidationFailedException;
import com.ustmd.labwork3.model.Codes;
import com.ustmd.labwork3.model.ErrorCodes;
import com.ustmd.labwork3.model.ErrorMessages;
import com.ustmd.labwork3.model.Request;
import com.ustmd.labwork3.model.Response;
import com.ustmd.labwork3.service.AnnualBonusService;
import com.ustmd.labwork3.service.ModifyRequestService;
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
    private final ModifyRequestService modifySourceRequestService;
    private final ModifyRequestService modifySystemNameRequestService;
    private final AnnualBonusService annualBonusService;

    public MyController(ValidationService validationService,
            @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService,
            @Qualifier("ModifySourceRequestService") ModifyRequestService modifySourceRequestService,
            @Qualifier("ModifySystemNameRequestService") ModifyRequestService modifySystemNameRequestService,
            AnnualBonusService annualBonusService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifySourceRequestService = modifySourceRequestService;
        this.modifySystemNameRequestService = modifySystemNameRequestService;
        this.annualBonusService = annualBonusService;
    }

    @PostMapping(value = "/feedback", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request,
            BindingResult bindingResult) {
        log.info("Получен request: {}", request);
        logElapsedTime(request);
        Response response = Response.builder()
                .uid(request.getUid() == null ? "" : request.getUid())
                .operationUid(request.getOperationUid() == null ? "" : request.getOperationUid())
                .systemTime(DateTimeUtil.getCustomFormat().format(new Date()))
                .code(Codes.SUCCESS)
                .errorCode(ErrorCodes.EMPTY)
                .errorMessage(ErrorMessages.EMPTY)
                .build();
        try {
            validationService.isValid(bindingResult);
            if ("123".equals(request.getUid())) {
                throw new UnsupportedCodeException(ErrorMessages.UNSUPPORTED.getName());
            }
            response.setAnnualBonus(annualBonusService.calculate(request.getPosition(),
                    request.getSalary(), request.getBonus(), request.getWorkDays()));
            if (request.getPosition().isManager()) {
                response.setQuarterlyBonus(annualBonusService.calculateQuarterlyBonus(request.getPosition(),
                        request.getSalary(), request.getBonus(), request.getWorkDays()));
            }
            response = modifyResponseService.modify(response);
            modifySourceRequestService.modify(request);
            modifySystemNameRequestService.modify(request);
        } catch (ValidationFailedException exception) {
            return error(response, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION, HttpStatus.BAD_REQUEST);
        } catch (UnsupportedCodeException exception) {
            return error(response, ErrorCodes.UNSUPPORTED_EXCEPTION, ErrorMessages.UNSUPPORTED, HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            log.error("uid={}: непредвиденная ошибка обработки request", request.getUid(), exception);
            return error(response, ErrorCodes.UNKNOWN_EXCEPTION, ErrorMessages.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("uid={}: отправлен response: {}", request.getUid(), response);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Response> error(Response response, ErrorCodes errorCode,
            ErrorMessages errorMessage, HttpStatus status) {
        response.setCode(Codes.FAILED);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        response.setAnnualBonus(null);
        response.setQuarterlyBonus(null);
        log.error("uid={}: отправлен response, HTTP {}: {}", response.getUid(), status.value(), response);
        return ResponseEntity.status(status).body(response);
    }

    private void logElapsedTime(Request request) {
        try {
            long sendTime = DateTimeUtil.getCustomFormat().parse(request.getSystemTime()).getTime();
            log.info("uid={}: время от отправки request до получения Сервисом 1: {} мс",
                    request.getUid(), System.currentTimeMillis() - sendTime);
        } catch (ParseException | NullPointerException exception) {
            log.warn("uid={}: время не вычислено, некорректный systemTime: {}",
                    request.getUid(), request.getSystemTime());
        }
    }
}
