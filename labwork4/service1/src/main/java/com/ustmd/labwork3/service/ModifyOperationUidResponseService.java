package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Response;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Qualifier("ModifyOperationUidResponseService")
public class ModifyOperationUidResponseService implements ModifyResponseService {

    @Override
    public Response modify(Response response) {
        String previousOperationUid = response.getOperationUid();
        response.setOperationUid(UUID.randomUUID().toString());
        log.info("uid={}: изменен response.operationUid: {} -> {}",
                response.getUid(), previousOperationUid, response.getOperationUid());
        return response;
    }
}
