package org.example.gemini;

import lombok.Getter;

@Getter
public class GeminiResult {
    private final String json;
    private final boolean isValid;

    public GeminiResult(String json, boolean isValid) {
        this.json = json;
        this.isValid = isValid;
    }
}
