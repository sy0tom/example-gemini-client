package org.example.repository;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import lombok.RequiredArgsConstructor;
import org.example.gemini.GeminiClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Qualifier("translateGeminiClient")
public class GeminiTranslateRepositoryImpl extends GeminiRepositoryAbstract implements TranslateRepository {

    private final GeminiClient geminiClient;

    @Override
    public String translate() {
        final int retryMax = 3;
        return callGenerateContent(retryMax, () -> geminiClient.generateContent(getInputContents()), String.class, null);
    }

    private List<Content> getInputContents() {
        return List.of(Content.newBuilder()
                .setRole("user")
                .addAllParts(List.of(
                        Part.newBuilder().setText("helloを翻訳してください。出力はjson形式にしてください。").build()
                ))
                .build());
    }
}
