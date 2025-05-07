package org.example.repository;

import org.example.model.ResultHolderModel;
import org.example.model.TranslatePromptModel;
import org.example.model.TranslateResultModel;

import javax.annotation.Nonnull;

public interface TranslateRepository {

  ResultHolderModel<TranslateResultModel> translate(@Nonnull TranslatePromptModel prompt);

  ResultHolderModel<TranslateResultModel> translateWithRetry(@Nonnull TranslatePromptModel prompt);
}
