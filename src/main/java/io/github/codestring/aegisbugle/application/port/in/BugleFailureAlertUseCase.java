package io.github.codestring.aegisbugle.application.port.in;

import io.github.codestring.aegisbugle.application.core.BugleAlertException;
import io.github.codestring.aegisbugle.application.core.model.BugleEvent;

public interface BugleFailureAlertUseCase {
    void raiseFailureAlert(BugleEvent event) throws BugleAlertException;
}
