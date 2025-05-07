package org.example.gemini;

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
        serviceAccountCredentialProvider.setScope(geminiProperties.getCredentialsScope());
        return new GeminiClient(
                geminiProperties.getProjectId(),
                geminiProperties.getTransport(),
                serviceAccountCredentialProvider,
                geminiProperties.getTasks().get(GeminiTasks.TRANSLATE.name().toLowerCase()));
    }
}
