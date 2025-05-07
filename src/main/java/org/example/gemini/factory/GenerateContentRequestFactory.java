package org.example.gemini.factory;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentRequest;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.SafetySetting;

import javax.annotation.Nonnull;
import java.util.List;

public class GenerateContentRequestFactory {
  public static GenerateContentRequest create(
    @Nonnull String model,
    @Nonnull GenerationConfig generationConfig,
    @Nonnull List<SafetySetting> safetySettings,
    @Nonnull List<Content> contents
  ) {
    return GenerateContentRequest.newBuilder()
      .setModel(model)
      .setGenerationConfig(generationConfig)
      .addAllSafetySettings(safetySettings)
      .addAllContents(contents)
      .build();
  }
}
