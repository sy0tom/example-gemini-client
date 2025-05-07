package org.example;

import lombok.RequiredArgsConstructor;
import org.example.service.TranslateService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Qualifier("translateGeminiClient")
public class Main implements CommandLineRunner {

  private final TranslateService translateService;

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Override
  public void run(String... args) {
    System.out.println(translateService.translate("おはよう").getEn());
    System.out.println(translateService.translateWithRetry("おやすみ").getKo());
  }
}