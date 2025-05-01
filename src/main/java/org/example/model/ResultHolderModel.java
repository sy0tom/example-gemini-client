package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class ResultHolderModel<T> {

    @Getter
    @AllArgsConstructor
    public static class SuccessResult<T> {
        private String json;
        private T data;
    }

    @Setter
    private SuccessResult<T> successResult;
    private final List<String> failedResultJsons = new ArrayList<>();

    public void addFailedResult(String json) {
        failedResultJsons.add(json);
    }
}
