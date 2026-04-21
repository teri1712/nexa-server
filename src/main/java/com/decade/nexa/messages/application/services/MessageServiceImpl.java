package com.decade.nexa.messages.application.services;

import com.decade.nexa.messages.application.ports.out.MessageRepository;
import com.decade.nexa.messages.application.query.MessageService;
import com.decade.nexa.messages.dto.MessageMapper;
import com.decade.nexa.messages.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messages;
    private final MessageMapper mapper;

    @Override
    public List<MessageResponse> getMessages(UUID userId, Long anchorSeq) {
        return messages.findTop20ByUserIdAndSequenceIdLessThan(userId, anchorSeq, Sort.by(Sort.Order.desc("sequenceId")))
            .stream()
            .map(mapper::toDto)
            .toList()
            ;
    }
}
