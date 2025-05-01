package org.example.model;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Type;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@Getter
public class TranslatePromptModel {

    private static final String PROMPT_FORMAT = """
            [指示]
            %sを英語と韓国語に翻訳してください。
            出力は[出力例]に従ってjsonで出力してください。
            
            [出力例1]
            {
              "JP": "こんにちは",
              "EN": "Hello",
              "KO": "안녕하세요"
            }
            
            [出力例2]
            {
              "JP": "ありがとう",
              "EN": "Thanks you",
              "KO": "감사합니다"
            }
            """;
    public static final Schema responseSchema = Schema.newBuilder()
            .setType(Type.OBJECT)
            .putAllProperties(Map.of(
                    "JP", Schema.newBuilder().setType(Type.STRING).build(),
                    "EN", Schema.newBuilder().setType(Type.STRING).build(),
                    "KO", Schema.newBuilder().setType(Type.STRING).build()
            ))
            .build();

    private final List<Content> contents;

    public TranslatePromptModel(@Nonnull String text) {
        this.contents = buildContents(text);
    }

    private List<Content> buildContents(@Nonnull String text) {
        return List.of(Content.newBuilder()
                .setRole("user")
                .addAllParts(List.of(
                        Part.newBuilder().setText(String.format(PROMPT_FORMAT, text)).build()
                ))
                .build());
    }
}
