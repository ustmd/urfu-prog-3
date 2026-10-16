package com.ustmd.labwork3.service;

import com.ustmd.labwork3.model.Response;
import com.ustmd.labwork3.util.DateTimeUtil;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Qualifier("ModifySystemTimeResponseService")
public class ModifySystemTimeResponseService implements ModifyResponseService {

    @Override
    public Response modify(Response response) {
        String previousTime = response.getSystemTime();
        response.setSystemTime(DateTimeUtil.getCustomFormat().format(new Date()));
        log.info("uid={}: изменен response.systemTime: {} -> {}",
                response.getUid(), previousTime, response.getSystemTime());
        return response;
    }
}
