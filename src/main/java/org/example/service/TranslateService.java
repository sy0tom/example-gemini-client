package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.ResultHolderModel;
import org.example.model.TranslatePromptModel;
import org.example.model.TranslateResultModel;
import org.example.repository.TranslateRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TranslateService {

  private final TranslateRepository translateRepository;


  public TranslateResultModel translate(@Nonnull String text) {
    return handleTranslateResult(translateRepository.translate(new TranslatePromptModel(text)));
  }

  public TranslateResultModel translateWithRetry(@Nonnull String text) {
    return handleTranslateResult(
        translateRepository.translateWithRetry(new TranslatePromptModel(text)));
  }

  private TranslateResultModel handleTranslateResult(
      @Nonnull ResultHolderModel<TranslateResultModel> result) {
    if (!result.getFailedResultJsons().isEmpty()) {
      result.getFailedResultJsons().forEach(it -> write(false, it));
    }

    if (Objects.isNull(result.getSuccessResult())) {
      throw new RuntimeException("Translate task is failed.");
    }

    write(true, result.getSuccessResult().getJson());

    return result.getSuccessResult().getData();
  }

  private void write(boolean isSuccess, String json) {
    System.out.printf("result: %s data: %s%n", isSuccess, json);
  }
}
