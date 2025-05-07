package org.example.gemini;

import com.google.cloud.vertexai.Transport;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@Data
@ConfigurationProperties("gcp.service.gemini")
public class GeminiProperties {

  @Setter
  public static class GeminiTaskProperties {

    @Getter
    private String modelName;
    @Getter
    private float temperature;
    @Getter
    private float topP;
    @Getter
    private int maxAttempts;
    private int totalTimeoutSeconds;
    private int logicalTimeoutSeconds;
    private int initialRetryDelaySeconds;
    private int maxRetryDurationSeconds;
    @Getter
    private Double retryDelayMultiplier;
    private int rpcInitialTimeoutSeconds;
    private int maxRpcTimeoutSeconds;
    @Getter
    private Double rpcTimeoutMultiplier;

    public Duration getTotalTimeout() {
      return Duration.ofSeconds(totalTimeoutSeconds);
    }

    public Duration getLogicalTimeout() {
      return Duration.ofSeconds(logicalTimeoutSeconds);
    }

    public Duration getInitialRetryDelay() {
      return Duration.ofSeconds(initialRetryDelaySeconds);
    }

    public Duration getMaxRetryDuration() {
      return Duration.ofSeconds(maxRetryDurationSeconds);
    }

    public Duration getRpcInitialTimeout() {
      return Duration.ofSeconds(rpcInitialTimeoutSeconds);
    }

    public Duration getMaxRpcTimeout() {
      return Duration.ofSeconds(maxRpcTimeoutSeconds);
    }
  }

  private String credentialsScope;
  private String projectId;
  private String location;
  private Transport transport;
  private Map<String, GeminiTaskProperties> tasks;
}
