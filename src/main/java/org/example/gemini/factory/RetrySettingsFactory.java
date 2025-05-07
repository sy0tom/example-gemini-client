package org.example.gemini.factory;

import com.google.api.gax.retrying.RetrySettings;

import javax.annotation.Nonnull;
import java.time.Duration;

public class RetrySettingsFactory {

  public static RetrySettings create(
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
}
