package io.github.codestring.aegisbugle.application.core.service;

import io.github.codestring.aegisbugle.adapter.out.AlertMapper;
import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.AlertEvent;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;
import io.github.codestring.aegisbugle.application.port.in.BugleFailureAlertUseCase;
import io.github.codestring.aegisbugle.application.port.out.BuglePublisher;
import io.github.codestring.aegisbugle.config.BugleProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class BugleAlertService implements BugleFailureAlertUseCase {

    private final BugleProperties properties;
    private final BuglePublisher buglePublisher;
    private final AlertMapper alertMapper;


    public void raiseFailureAlert(BugleEvent event) throws BugleAlertException {
        validateEventMessage(event);

        AlertEvent alert = alertMapper.toAlertEvent(event);
        alert.setServiceName(properties.getServiceName());
        alert.setAlertId();
        alert.setEnvironment(properties.getEnvironment().name());
        buglePublisher.sendAlert(alert, event.getTopic());
    }

    private static void validateEventMessage(BugleEvent event) throws BugleAlertException {
        if(StringUtils.isEmpty(event.getTopic()) || StringUtils.isBlank(event.getTopic())){
            throw new BugleAlertException("Invalid topic provided");
        }else if(StringUtils.isEmpty(event.getErrorCode()) || StringUtils.isBlank(event.getErrorCode())){
            throw new BugleAlertException("Invalid error code provided");
        }else if(StringUtils.isEmpty(event.getErrorMessage()) || StringUtils.isBlank(event.getErrorMessage())){
            throw new BugleAlertException("Invalid error message provided");
        }else if(StringUtils.isEmpty(event.getExceptionType()) || StringUtils.isBlank(event.getExceptionType())){
            throw new BugleAlertException("Invalid exception type provided");
        }else if(StringUtils.isEmpty(event.getSeverity().name()) || StringUtils.isBlank(event.getSeverity().name())){
            throw new BugleAlertException("Invalid severity provided");
        }

    }


}
