package org.example.gemini.factory;

import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.vertexai.Transport;
import com.google.cloud.vertexai.api.PredictionServiceSettings;

import javax.annotation.Nonnull;
import java.io.IOException;

public class PredictionServiceSettingsFactory {

    public static PredictionServiceSettings create(
            @Nonnull Transport transport,
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
}
