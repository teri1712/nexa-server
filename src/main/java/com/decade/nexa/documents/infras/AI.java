package com.decade.nexa.documents.infras;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

public interface AI {

    ChatClient.ChatClientRequestSpec suggest(Prompt prompt);

}
