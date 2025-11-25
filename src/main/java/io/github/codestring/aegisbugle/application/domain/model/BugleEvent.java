package io.github.codestring.aegisbugle.application.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BugleEvent {
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

    private String topicName;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
