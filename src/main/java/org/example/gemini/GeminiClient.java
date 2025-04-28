package org.example.gemini;

import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.StatusCode;
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
import com.google.common.base.Supplier;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public class GeminiClient {
    private final String projectId;
    private final String location;
    private final Transport transport;
    private final GoogleCredentials credentials;
    private final GeminiProperties.GeminiTaskProperties geminiTaskProperties;
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
            @Nonnull GeminiProperties.GeminiTaskProperties geminiTaskProperties
    ) throws IOException {
        this.projectId = projectId;
        this.location = location;
        this.transport = transport;
        this.credentials = credentials;
        this.geminiTaskProperties = geminiTaskProperties;
        this.predictionServiceSettings = buildPredictionServiceSettings(buildRetrySettings(
                geminiTaskProperties.getMaxAttempts(),
                geminiTaskProperties.getTotalTimeout(),
                geminiTaskProperties.getLogicalTimeout(),
                geminiTaskProperties.getInitialRetryDelay(),
                geminiTaskProperties.getMaxRetryDuration(),
                geminiTaskProperties.getRetryDelayMultiplier(),
                geminiTaskProperties.getRpcInitialTimeout(),
                geminiTaskProperties.getMaxRpcTimeout(),
                geminiTaskProperties.getRpcTimeoutMultiplier()
        ));
    }

    public GeminiResult generateContent(@Nonnull List<Content> contents) {
        return generateContent(contents, null);
    }

    public GeminiResult generateContent(
            @Nonnull List<Content> contents,
            Schema responseSchema
    ) {
        final Supplier<PredictionServiceClient> client = ()-> {
            try {
                return PredictionServiceClient.create(predictionServiceSettings);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        };
        try (
                final VertexAI vertexAI = new VertexAI.Builder()
                        .setProjectId(projectId)
                        .setLocation(location)
                        .setCredentials(credentials)
                        .setPredictionClientSupplier(client)
                        .build()
        ) {
            final GenerativeModel model = new GenerativeModel(geminiTaskProperties.getModelName(), vertexAI);
            model.withSafetySettings(safetySettings);
            model.withGenerationConfig(buildGenerateConfig(
                    this.geminiTaskProperties.getTemperature(),
                    this.geminiTaskProperties.getTopP(),
                    responseSchema));

            return createGeminiResult(model.generateContent(contents));
        } catch (final IOException e) {
            throw new UncheckedIOException("GeminiClient is failed for IOException.", e);
        }
    }

    private GeminiResult createGeminiResult(@Nonnull GenerateContentResponse response) {
        String json = "";
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
                    ).filter(it -> it >=0)
                    .max(Integer::compareTo)
                    .orElseThrow(() -> new IllegalArgumentException("json end keyword is not found."));

            return new GeminiResult(json.substring(start, end), true);
        } catch (final IndexOutOfBoundsException | IllegalArgumentException e) {
            log.warn("GeminiResult parse failed. json={}, message={}", json, e.getMessage());
            return new GeminiResult(json, false);
        }
    }

    private GenerationConfig buildGenerateConfig(
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

    private PredictionServiceSettings buildPredictionServiceSettings(
            @Nonnull RetrySettings retrySettings
    ) throws IOException {

        final PredictionServiceSettings.Builder builder = switch (transport) {
            case GRPC -> PredictionServiceSettings.newBuilder();
            case REST -> PredictionServiceSettings.newHttpJsonBuilder();
        };

        builder.predictSettings()
                .setRetrySettings(retrySettings)
                .setRetryableCodes(StatusCode.Code.UNKNOWN, StatusCode.Code.INTERNAL, StatusCode.Code.UNAVAILABLE);
        return builder.build();
    }

    private static RetrySettings buildRetrySettings(
            int maxAttempts,
            @Nonnull Duration totalTimeout,
            @Nonnull Duration logicalTimeout,
            @Nonnull Duration initialRetryDelay,
            @Nonnull Duration maxRetryDuration,
            @Nonnull Double retryDelayMultiplier,
            @Nonnull Duration rpcInitialTimeout,
            @Nonnull Duration maxRpcTimeout,
            @Nonnull Double rpcTimeoutMultiplier
    ) {
        // todo リトライ設定について調査する
        return RetrySettings.newBuilder()
                .setMaxAttempts(maxAttempts)
                .setTotalTimeoutDuration(totalTimeout)
                //.setLogicalTimeout(logicalTimeout)
                .setInitialRetryDelayDuration(initialRetryDelay)
                .setMaxRetryDelayDuration(maxRetryDuration)
                .setRetryDelayMultiplier(retryDelayMultiplier)
                .setInitialRpcTimeoutDuration(rpcInitialTimeout)
                .setMaxRpcTimeoutDuration(maxRpcTimeout)
                .setRpcTimeoutMultiplier(rpcTimeoutMultiplier)
                .build();
    }

    private static SafetySetting buildSafetySetting(@Nonnull HarmCategory harmCategory) {
        return SafetySetting.newBuilder()
                .setCategory(harmCategory)
                .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                .build();
    }
}
