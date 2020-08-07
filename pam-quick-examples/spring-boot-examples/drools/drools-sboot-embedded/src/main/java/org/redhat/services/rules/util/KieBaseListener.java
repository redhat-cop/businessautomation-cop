package org.redhat.services.rules.util;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.event.kiescanner.KieScannerEventListener;
import org.kie.api.event.kiescanner.KieScannerStatusChangeEvent;
import org.kie.api.event.kiescanner.KieScannerUpdateResultsEvent;
import org.redhat.services.model.dto.KJAR;

import java.util.Arrays;

@Slf4j
public class KieBaseListener implements KieScannerEventListener {

    private KJAR kjar;

    public KieBaseListener(KJAR kjar) {
        this.kjar = kjar;
    }

    @Override
    public void onKieScannerStatusChangeEvent(KieScannerStatusChangeEvent statusChange) {
        log.info("KIE Event Listener Status Change: {} for {}", statusChange.getStatus(), kjar);
    }

    @Override
    public void onKieScannerUpdateResultsEvent(KieScannerUpdateResultsEvent updateResults) {
        log.info("KIE Event Listener Status Results: {} for {}", Arrays.asList(updateResults.getResults().getMessages()), kjar);
    }

}
