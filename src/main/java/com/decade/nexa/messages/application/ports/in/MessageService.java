package com.decade.nexa.messages.application.ports.in;

import com.decade.nexa.messages.dto.MessageDto;
import com.decade.nexa.messages.dto.MessagePlacedDto;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    List<MessageDto> getMessages(UUID userId, Long anchorSeq);

    MessagePlacedDto addMessage(UUID userId, String message);

}
