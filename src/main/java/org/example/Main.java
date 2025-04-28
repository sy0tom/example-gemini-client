package org.example;

import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import lombok.RequiredArgsConstructor;
import org.example.gemini.GeminiClient;
import org.example.gemini.GeminiResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@Qualifier("translateGeminiClient")
public class Main implements CommandLineRunner {
    private final GeminiClient geminiClient;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Hello, World!");
    }

    @Override
    public void run(String... args) {
        GeminiResult geminiResult = geminiClient.generateContent(getInputContents());
        System.out.println(geminiResult.json());
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