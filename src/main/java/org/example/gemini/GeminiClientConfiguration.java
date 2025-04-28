package org.example.gemini;

import com.google.auth.oauth2.GoogleCredentials;
import org.example.provider.ServiceAccountCredentialProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiClientConfiguration {
    private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private final GoogleCredentials googleCredentials;
    public GeminiClientConfiguration(@Nonnull ServiceAccountCredentialProvider serviceAccountCredentialProvider) {
        googleCredentials = serviceAccountCredentialProvider.getByScope(SCOPE);
    }

    @Bean("HogeGeminiClient")
    public GeminiClient getHogeGeminiClient(
            @Nonnull GeminiProperties geminiProperties
    ) throws IOException {
        return new GeminiClient(
                geminiProperties.getProjectId(),
                geminiProperties.getLocation(),
                geminiProperties.getEndpoint(),
                googleCredentials,
                geminiProperties.getTasks().get("hoge"));
    }
}
