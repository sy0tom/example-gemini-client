package org.example.gemini;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentRequest;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.api.Schema;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.GeminiResultInvalidException;
import org.example.gemini.factory.GenerateContentRequestFactory;
import org.example.gemini.factory.GenerationConfigFactory;
import org.example.gemini.factory.PredictionServiceSettingsFactory;
import org.example.gemini.factory.RetrySettingsFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Slf4j
public class GeminiClient {

  private final String endpoint;
  private final float temperature;
  private final float topP;
  private final PredictionServiceSettings predictionServiceSettings;

  private static final List<SafetySetting> safetySettings = Stream.of(
          HarmCategory.HARM_CATEGORY_HATE_SPEECH, HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
          HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT, HarmCategory.HARM_CATEGORY_HARASSMENT)
      .map(GeminiClient::buildSafetySetting).toList();

  private static final String ENDPOINT_FORMAT = "projects/%s/locations/%s/publishers/google/models/%s";

  public GeminiClient(
      @Nonnull String projectId,
      @Nonnull String location,
      @Nonnull Transport transport,
      @Nonnull CredentialsProvider credentialsProvider,
      @Nonnull GeminiProperties.GeminiTaskProperties taskProps
  ) throws IOException {
    this.endpoint = String.format(ENDPOINT_FORMAT, projectId, location, taskProps.getModelName());
    this.temperature = taskProps.getTemperature();
    this.topP = taskProps.getTopP();
    this.predictionServiceSettings = PredictionServiceSettingsFactory.create(
        transport,
        credentialsProvider,
        RetrySettingsFactory.create(
            taskProps.getMaxAttempts(),
            taskProps.getTotalTimeout(),
            taskProps.getInitialRetryDelay(),
            taskProps.getMaxRetryDuration(),
            taskProps.getRetryDelayMultiplier(),
            taskProps.getRpcInitialTimeout(),
            taskProps.getMaxRpcTimeout(),
            taskProps.getRpcTimeoutMultiplier()));
  }

  public String generateContent(@Nonnull List<Content> contents) {
    return generateContent(contents, null);
  }

  public String generateContent(@Nonnull List<Content> contents, Schema responseSchema) {
    ApiFuture<GenerateContentResponse> futureCall;
    try (final PredictionServiceClient client = PredictionServiceClient.create(
        predictionServiceSettings)) {
      final GenerateContentRequest request = GenerateContentRequestFactory.create(
          endpoint, GenerationConfigFactory.create(temperature, topP, responseSchema),
          safetySettings, contents);

      futureCall = client.generateContentCallable().futureCall(request);

      return getAndTrimJson(futureCall.get());
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  private String getAndTrimJson(@Nonnull GenerateContentResponse response) {
    String json = null;
    try {
      json = response.getCandidates(0).getContent().getParts(0).getText();
      final int start = Stream.of(json.indexOf("{"), json.indexOf("["))
          .filter(it -> it >= 0)
          .min(Integer::compareTo)
          .orElseThrow(() -> new IllegalArgumentException("json start keyword is not found."));

      final int end = Stream.of(json.lastIndexOf("}"), json.lastIndexOf("]"))
                          .filter(it -> it >= 0)
                          .max(Integer::compareTo)
                          .orElseThrow(
                              () -> new IllegalArgumentException("json end keyword is not found."))
                      + 1;

      return json.substring(start, end);
    } catch (final IndexOutOfBoundsException | IllegalArgumentException e) {
      throw new GeminiResultInvalidException(json, e);
    }
  }

  private static SafetySetting buildSafetySetting(@Nonnull HarmCategory harmCategory) {
    return SafetySetting.newBuilder()
        .setCategory(harmCategory)
        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
        .build();
  }
}
