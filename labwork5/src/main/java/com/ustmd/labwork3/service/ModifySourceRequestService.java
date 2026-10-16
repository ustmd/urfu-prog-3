package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Qualifier("ModifySourceRequestService")
public class ModifySourceRequestService implements ModifyRequestService {

    @Override
    public void modify(Request request) {
        String previousSource = request.getSource();
        request.setSource("Service 1");
        log.info("uid={}: изменен request.source: {} -> {}",
                request.getUid(), previousSource, request.getSource());
    }
}
