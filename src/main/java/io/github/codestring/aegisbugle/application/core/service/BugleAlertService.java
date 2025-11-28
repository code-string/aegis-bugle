package io.github.codestring.aegisbugle.application.core.service;

import io.github.codestring.aegisbugle.adapter.out.AlertMapper;
import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BugleAlertService {

    private final BugleProperties properties;
    private final BuglePublisher buglePublisher;
    private final AlertMapper alertMapper;


    public void raiseFailureAlert(BugleEvent event) throws BugleAlertException {
        AlertEvent alert = alertMapper.toAlertEvent(event);
        alert.setServiceName(properties.getServiceName());
        alert.setAlertId();
        alert.setEnvironment(properties.getEnvironment().name());
        buglePublisher.sendAlert(alert, event.getTopicName());
    }


}
