package org.redhat.services.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralUtil {

    /**
     * Test Spring Injection into non-Spring bean (LoggingEventEmitter)
     * @param message
     */
    public void logThisForMe(String message){
        log.info(message);
    }
}
