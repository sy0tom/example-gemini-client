package org.example.provider;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Setter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(ServiceAccountCredentialProperties.class)
public class ServiceAccountCredentialProvider implements CredentialsProvider {

    private final ServiceAccountCredentials serviceAccountCredentials;
    @Setter
    private String scope;

    public ServiceAccountCredentialProvider(
            @Nonnull ServiceAccountCredentialProperties properties
    ) {
        try {
            this.serviceAccountCredentials = ServiceAccountCredentials.fromStream(
                    new ByteArrayInputStream(properties.getCredentialsValue().getBytes(StandardCharsets.UTF_8)));
        } catch (final IOException e) {
            throw new UncheckedIOException("GCP Credential load failed.", e);
        }
    }

    @Override
    public Credentials getCredentials() {
        if (Objects.nonNull(scope)) {
            return serviceAccountCredentials.createScoped(scope);
        }
        return serviceAccountCredentials;
    }
}
