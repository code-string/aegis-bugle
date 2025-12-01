package io.github.codestring.aegisbugle.application.core.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FailureMessage {
    private String originalDestination;
    private Object message;
    private String errorMessage;
    private String errorClass;
    private Long timestamp;
}
