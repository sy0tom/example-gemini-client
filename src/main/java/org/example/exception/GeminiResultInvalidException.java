package org.example.exception;

import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class GeminiResultInvalidException extends RuntimeException {
    private final String json;
    public GeminiResultInvalidException(String json, @Nonnull Throwable throwable) {
        super(throwable);
        this.json = json;
    }

    public GeminiResultInvalidException(String json) {
        super();
        this.json = json;
    }
}
