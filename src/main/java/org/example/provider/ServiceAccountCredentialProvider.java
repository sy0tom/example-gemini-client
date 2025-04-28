package org.example.provider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@Configuration
public class ServiceAccountCredentialProvider {

    private final ServiceAccountCredentials serviceAccountCredentials;

    public ServiceAccountCredentialProvider() throws IOException {
        this.serviceAccountCredentials = ServiceAccountCredentials.fromStream(
                new ByteArrayInputStream(System.getenv("GOOGLE_APPLICATION_CREDENTIAL_VALUE").getBytes(StandardCharsets.UTF_8)));
    }

    public GoogleCredentials getByScope(@Nonnull String scope) {
        return serviceAccountCredentials.createScoped(scope);
    }
}
