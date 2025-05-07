package org.example.gemini.provider;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import lombok.Data;
import org.example.gemini.GeminiProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class GeminiCredentialsProvider implements CredentialsProvider {
  private final ServiceAccountCredentials serviceAccountCredentials;
  private final String scope;
  public GeminiCredentialsProvider(
      @Nonnull ServiceAccountCredentialProperties serviceAccountCredentialProperties,
      @Nonnull GeminiProperties geminiProperties
  ) {
    try {
      this.serviceAccountCredentials = ServiceAccountCredentials.fromStream(
          new ByteArrayInputStream(
              serviceAccountCredentialProperties.getCredentialsValue().getBytes(StandardCharsets.UTF_8)));
      this.scope = geminiProperties.getCredentialsScope();
    } catch (final IOException e) {
      throw new UncheckedIOException("GCP Credential load failed.", e);
    }
  }

  @Override
  public Credentials getCredentials() {
    return serviceAccountCredentials.createScoped(scope);
  }
}