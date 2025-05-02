package org.example.gemini;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.GeminiResultInvalidException;
import org.example.gemini.factory.GenerationConfigFactory;
import org.example.gemini.factory.PredictionServiceSettingsFactory;
import org.example.gemini.factory.RetrySettingsFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class GeminiClient {
    private final String projectId;
    private final String location;
    private final Transport transport;
    private final GoogleCredentials credentials;
    private final String modelName;
    private final float temperature;
    private final float topP;
    private final PredictionServiceSettings predictionServiceSettings;

    private static final List<SafetySetting> safetySettings = Stream.of(
            HarmCategory.HARM_CATEGORY_HATE_SPEECH,
            HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT,
            HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT,
            HarmCategory.HARM_CATEGORY_HARASSMENT
    ).map(GeminiClient::buildSafetySetting).toList();

    public GeminiClient(
            @Nonnull String projectId,
            @Nonnull String location,
            @Nonnull Transport transport,
            @Nonnull GoogleCredentials credentials,
            @Nonnull GeminiProperties.GeminiTaskProperties taskProps
    ) throws IOException {
        this.projectId = projectId;
        this.location = location;
        this.transport = transport;
        this.credentials = credentials;
        this.modelName = taskProps.getModelName();
        this.temperature = taskProps.getTemperature();
        this.topP = taskProps.getTopP();
        this.predictionServiceSettings = PredictionServiceSettingsFactory.create(transport, RetrySettingsFactory.create(
                taskProps.getMaxAttempts(),
                taskProps.getTotalTimeout(),
                taskProps.getLogicalTimeout(),
                taskProps.getInitialRetryDelay(),
                taskProps.getMaxRetryDuration(),
                taskProps.getRetryDelayMultiplier(),
                taskProps.getRpcInitialTimeout(),
                taskProps.getMaxRpcTimeout(),
                taskProps.getRpcTimeoutMultiplier()
        ));
    }

    public String generateContent(@Nonnull List<Content> contents) {
        return generateContent(contents, null);
    }

    public String generateContent(
            @Nonnull List<Content> contents,
            Schema responseSchema
    ) {
        try (final VertexAI vertexAI = buildVertexAi()) {
            return getAndTrimJson(buildModel(vertexAI,
                    GenerationConfigFactory.create(temperature, topP, responseSchema)).generateContent(contents));
        } catch (final IOException e) {
            throw new UncheckedIOException("GeminiClient is failed for IOException.", e);
        }
    }

    private VertexAI buildVertexAi() {
        return new VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .setTransport(transport)
                .setCredentials(credentials)
                .setPredictionClientSupplier(
                        () -> {
                            try {
                                return PredictionServiceClient.create(predictionServiceSettings);
                            } catch (final IOException e) {
                                throw new UncheckedIOException("Failed build VertexAI.", e);
                            }
                        }
                ).build();
    }

    private GenerativeModel buildModel(
            @Nonnull VertexAI vertexAI,
            @Nonnull GenerationConfig generationConfig
    ) {
        final GenerativeModel model = new GenerativeModel(modelName, vertexAI);
        model.withSafetySettings(safetySettings);
        model.withGenerationConfig(generationConfig);
        return model;
    }

    private String getAndTrimJson(@Nonnull GenerateContentResponse response) {
        String json = null;
        try {
            json = response.getCandidates(0).getContent().getParts(0).getText();
            final int start = Stream.of(
                            json.indexOf("{"),
                            json.indexOf("[")
                    )
                    .filter(it -> it >= 0)
                    .min(Integer::compareTo)
                    .orElseThrow(() -> new IllegalArgumentException("json start keyword is not found."));

            final int end = Stream.of(
                            json.lastIndexOf("}"),
                            json.lastIndexOf("]")
                    ).filter(it -> it >= 0)
                    .max(Integer::compareTo)
                    .orElseThrow(() -> new IllegalArgumentException("json end keyword is not found.")) + 1;

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
