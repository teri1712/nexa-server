package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.in.MessageService;
import com.decade.nexa.messages.application.ports.out.MessageRepository;
import com.decade.nexa.messages.domain.AgentMessage;
import com.decade.nexa.messages.domain.UserMessage;
import com.decade.nexa.messages.dto.MessageDto;
import com.decade.nexa.messages.dto.MessageMapper;
import com.decade.nexa.messages.dto.MessagePlacedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messages;
    private final MessageMapper mapper;

    @Override
    public List<MessageDto> getMessages(UUID userId, Long anchorSeq) {
        return messages.findTop20ByUserIdAndSequenceIdLessThan(userId, anchorSeq, Sort.by(Sort.Order.desc("sequenceNumber")))
            .stream()
            .map(mapper::toDto)
            .toList();
    }

    @Override
    public MessagePlacedDto addMessage(UUID userId, String message) {
        UserMessage userMessage = new UserMessage(message, userId);
        messages.save(userMessage);
        AgentMessage agentMessage = new AgentMessage(null, userId);
        messages.save(agentMessage);
        return new MessagePlacedDto(mapper.toDto(userMessage), agentMessage.getSequenceId());
    }
}
