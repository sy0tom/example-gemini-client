package org.example.gemini;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.example.provider.ServiceAccountCredentialProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
@RequiredArgsConstructor
public class GeminiClientConfiguration {

    private final ServiceAccountCredentialProvider serviceAccountCredentialProvider;

    @Bean("translateGeminiClient")
    public GeminiClient getTranslateGeminiClient(
            @Nonnull GeminiProperties geminiProperties
    ) throws IOException {
        return new GeminiClient(
                geminiProperties.getProjectId(),
                geminiProperties.getLocation(),
                geminiProperties.getTransport(),
                serviceAccountCredentialProvider.getCredentialsByScope(geminiProperties.getCredentialsScope()),
                geminiProperties.getTasks().get(GeminiTasks.TRANSLATE.name().toLowerCase()));
    }
}
