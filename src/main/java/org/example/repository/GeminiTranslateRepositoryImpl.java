package org.example.repository;

import lombok.RequiredArgsConstructor;
import org.example.gemini.GeminiClient;
import org.example.model.ResultHolderModel;
import org.example.model.TranslatePromptModel;
import org.example.model.TranslateResultModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;

@Repository
@RequiredArgsConstructor
@Qualifier("translateGeminiClient")
public class GeminiTranslateRepositoryImpl extends GeminiRepositoryAbstract implements
    TranslateRepository {

  private final GeminiClient geminiClient;

  @Override
  public ResultHolderModel<TranslateResultModel> translate(@Nonnull TranslatePromptModel prompt) {
    return callGenerateContent(
        () -> geminiClient.generateContent(
            prompt.getContents(), TranslatePromptModel.responseSchema), TranslateResultModel.class,
        null);
  }

  @Override
  public ResultHolderModel<TranslateResultModel> translateWithRetry(
      @Nonnull TranslatePromptModel prompt) {
    final int retryMax = 3;
    return callGenerateContent(
        retryMax, () -> geminiClient.generateContent(
            prompt.getContents(), TranslatePromptModel.responseSchema), TranslateResultModel.class,
        null);
  }
}
