package org.example.gemini;

import lombok.RequiredArgsConstructor;
import org.example.gemini.provider.GeminiCredentialsProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
@RequiredArgsConstructor
public class GeminiClientConfiguration {

  private final GeminiProperties geminiProperties;
  private final GeminiCredentialsProvider geminiCredentialsProvider;

  @Bean("translateGeminiClient")
  public GeminiClient getTranslateGeminiClient() throws IOException {
    return new GeminiClient(
        geminiProperties.getProjectId(),
        geminiProperties.getLocation(),
        geminiProperties.getTransport(),
        geminiCredentialsProvider,
        geminiProperties.getTasks().get(GeminiTask.TRANSLATE.name().toLowerCase()));
  }
}
