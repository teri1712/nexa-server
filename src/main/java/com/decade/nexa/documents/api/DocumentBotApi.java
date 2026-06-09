package com.decade.nexa.documents.api;

import org.springframework.ai.chat.client.ChatClient;

public interface DocumentBotApi {
    ChatClient.ChatClientRequestSpec get(String docId);
}
