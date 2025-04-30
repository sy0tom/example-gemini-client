package org.example;

import lombok.RequiredArgsConstructor;
import org.example.repository.TranslateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Qualifier("translateGeminiClient")
public class Main implements CommandLineRunner {
   private final TranslateRepository translateRepository;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println(translateRepository.translate());
    }
}