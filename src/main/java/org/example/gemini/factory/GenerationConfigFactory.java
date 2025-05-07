package org.example.gemini.factory;

import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Schema;

import java.util.Objects;

public class GenerationConfigFactory {

  public static GenerationConfig create(
      float temperature,
      float topP,
      Schema responseSchema
  ) {
    final GenerationConfig.Builder builder = GenerationConfig.newBuilder()
        .setTemperature(temperature)
        .setTopP(topP);

    if (Objects.nonNull(responseSchema)) {
      builder.setResponseSchema(responseSchema);
      builder.setResponseMimeType("application/json");
    }

    return builder.build();
  }
}
