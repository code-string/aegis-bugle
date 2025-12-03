package io.github.codestring.aegisbugle;

import lombok.Getter;

@Getter
public class TestEvent {
    private final String id;
    private final String data;

    public TestEvent(String id, String data) {
        this.id = id;
        this.data = data;
    }
}
