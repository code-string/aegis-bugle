package io.github.codestring.aegisbugle.application.domain.service;

import io.github.codestring.aegisbugle.adapter.out.AlertMapper;
import io.github.codestring.aegisbugle.application.domain.BugleAlertException;
import io.github.codestring.aegisbugle.application.domain.model.AlertEvent;
import io.github.codestring.aegisbugle.application.domain.model.BugleEvent;
import io.github.codestring.aegisbugle.application.port.out.BugleProducer;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BugleAlertService {

    private final BugleProperties properties;
    private final BugleProducer bugleProducer;
    private final AlertMapper alertMapper;


    public void raiseCriticalFailureAlert(BugleEvent event) throws BugleAlertException {
        AlertEvent alert = alertMapper.toAlertEvent(event);
        alert.setServiceName(properties.getServiceName());
        alert.setAlertId();
        alert.setEnvironment(properties.getEnvironment().name());
        bugleProducer.sendAlert(alert, event.getTopicName());
    }
}
