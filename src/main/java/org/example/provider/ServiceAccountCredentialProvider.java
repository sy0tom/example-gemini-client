package org.example.provider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Getter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Getter
@Configuration
@EnableConfigurationProperties(ServiceAccountCredentialProperties.class)
public class ServiceAccountCredentialProvider {

    private final ServiceAccountCredentials serviceAccountCredentials;

    public ServiceAccountCredentialProvider(@Nonnull ServiceAccountCredentialProperties properties) {
        try {
            this.serviceAccountCredentials = ServiceAccountCredentials.fromStream(
                    new ByteArrayInputStream(properties.getCredentialsValue().getBytes(StandardCharsets.UTF_8)));
        } catch (final IOException e) {
            throw new UncheckedIOException("GCP Credential load failed.", e);
        }
    }

    public GoogleCredentials getCredentialsByScope(@Nonnull String scope) {
        return serviceAccountCredentials.createScoped(scope);
    }
}
