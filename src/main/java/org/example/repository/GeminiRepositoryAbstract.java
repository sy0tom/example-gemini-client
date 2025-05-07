package org.example.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.GeminiResultInvalidException;
import org.example.model.ResultHolderModel;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public abstract class GeminiRepositoryAbstract {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final int RETRY_WAIT_INTERVAL_MS = 500;

  protected <T> ResultHolderModel<T> callGenerateContent(
      @Nonnull Supplier<String> geminiContentSupplier,
      @Nonnull Class<T> responseClass,
      Validator<T> validator
  ) {
    return callGenerateContent(1, geminiContentSupplier, responseClass, validator);
  }

  protected <T> ResultHolderModel<T> callGenerateContent(
      int retryMax,
      @Nonnull Supplier<String> geminiContentSupplier,
      @Nonnull Class<T> responseClass,
      Validator<T> validator
  ) {
    final ResultHolderModel<T> result = new ResultHolderModel<>();

    int retryCount = 0;
    while (isRetryAble(retryCount++, retryMax)) {
      try {
        final String json = geminiContentSupplier.get();
        result.setSuccessResult(
            new ResultHolderModel.SuccessResult<>(json,
                parseAndValidate(json, responseClass, validator)));
        return result;
      } catch (final GeminiResultInvalidException e) {
        log.warn("Gemini result is invalid. json={}, message={}", e.getJson(), e.getMessage());
        result.addFailedResult(e.getJson());
      } catch (final StatusRuntimeException e) {
        final Status.Code code = e.getStatus().getCode();
        log.warn("Gemini api call failed. code={}", code);
        if (shouldWaitBeforeRetry(code)) {
          waitBeforeRetry(retryCount);
        }
      }
    }
    return result;
  }

  private <T> T parseAndValidate(String json, @Nonnull Class<T> clazz, Validator<T> validator) {
    try {
      final T result = OBJECT_MAPPER.readValue(json, clazz);
      if (Objects.nonNull(validator) && !validator.validate(result)) {
        throw new GeminiResultInvalidException(json);
      }
      return result;
    } catch (final IOException e) {
      throw new GeminiResultInvalidException(json, e);
    }
  }

  private static boolean isRetryAble(int retryCount, int retryMax) {
    return retryCount < retryMax;
  }

  private static boolean shouldWaitBeforeRetry(@Nonnull Status.Code code) {
    return Objects.equals(code, Status.Code.DEADLINE_EXCEEDED) || Objects.equals(code,
        Status.Code.RESOURCE_EXHAUSTED);
  }

  private static void waitBeforeRetry(int retryCount) {
    try {
      Thread.sleep((long) retryCount * RETRY_WAIT_INTERVAL_MS);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }

  @FunctionalInterface
  protected interface Validator<T> {

    boolean validate(T t);
  }
}
