package io.github.codestring.aegisbugle.application.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.codestring.aegisbugle.application.domain.BugleAlertException;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertEvent {
    @JsonProperty("alert_id")
    private String alertId;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("exception_type")
    private String exceptionType;

    @JsonProperty("stack_trace")
    private String stackTrace;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("severity")
    private AlertSeverity severity;

    @JsonProperty("environment")
    private String environment;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public void setAlertId() throws BugleAlertException {
        this.alertId = generateAlertId();
    }

    private String generateAlertId() throws BugleAlertException {
        if(serviceName == null || serviceName.isEmpty()) {
            throw new BugleAlertException("");
        }
        return "alert-".concat(serviceName.concat("-")) + Instant.now().toEpochMilli() + "-" + System.nanoTime();
    }
}
