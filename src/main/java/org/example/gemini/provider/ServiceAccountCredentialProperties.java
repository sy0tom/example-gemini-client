package org.example.gemini.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("gcp")
public class ServiceAccountCredentialProperties {

  private String credentialsValue;
}
