package com.decade.nexa.messages.application.query;

import com.decade.nexa.messages.dto.MessageResponse;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    List<MessageResponse> getMessages(UUID userId, Long anchorSeq);
}
