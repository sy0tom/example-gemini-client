package org.example.exception;

import javax.annotation.Nonnull;

public class GeminiExecuteFailedException extends RuntimeException {
    public GeminiExecuteFailedException(@Nonnull String message, @Nonnull Throwable throwable) {
        super(message, throwable);
    }
}
