package org.example.gemini;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@Data
@ConfigurationProperties("gemini")
public class GeminiProperties {

    @Data
    public static class GeminiTaskProperties {
        private String modelName;
        private float temperature;
        private float topP;
        private int retryMax;
        private int maxAttempts;
        private Duration totalTimeout;
        private Duration initialRetryDelay;
        private Duration maxRetryDuration;
        private Double retryDelayMultiplier;
        private Duration rpcInitialTimeout;
        private Duration maxRpcTimeout;
        private Double rpcTimeoutMultiplier;
    }

    private String projectId;
    private String location;
    private String endpoint;
    private Map<String, GeminiTaskProperties> tasks;
}
