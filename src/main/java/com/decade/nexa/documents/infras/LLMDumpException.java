package com.decade.nexa.documents.infras;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LLMDumpException extends Exception {
    private final String prompt;

    @Override
    public String getMessage() {
        return "Prompt: " + prompt + " failed";
    }
}
