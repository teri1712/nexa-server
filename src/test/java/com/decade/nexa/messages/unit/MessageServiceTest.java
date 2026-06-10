package com.decade.nexa.messages.unit;

import com.decade.nexa.messages.application.ports.out.MessageRepository;
import com.decade.nexa.messages.application.services.MessageServiceImpl;
import com.decade.nexa.messages.domain.AnswerMessage;
import com.decade.nexa.messages.domain.UserMessage;
import com.decade.nexa.messages.dto.MessageDto;
import com.decade.nexa.messages.dto.MessageMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    MessageRepository messages;

    @Spy
    MessageMapper mapper = Mappers.getMapper(MessageMapper.class);

    @InjectMocks
    MessageServiceImpl service;

    @Test
    void shouldSaveUserMessageAndPlaceHolderMessage() {
        String docId = "123";
        UUID userId = UUID.randomUUID();
        service.addMessage(docId, userId, "Hello");
        verify(messages).save(any(AnswerMessage.class));
        verify(messages).save(any(UserMessage.class));
    }

    @Test
    void placeHolderMessageShouldBeEmptyAndPointToAnswerMessage() {
        String docId = "123";
        UUID userId = UUID.randomUUID();
        Mockito.when(messages.save(any(AnswerMessage.class))).thenAnswer(invocation -> {
            AnswerMessage answer = invocation.getArgument(0);
            ReflectionTestUtils.setField(answer, "sequenceId", 123L);
            return answer;
        });
        Mockito.when(messages.save(any(UserMessage.class)))
            .thenAnswer(inn -> inn.getArgument(0));
        var placeholder = service.addMessage(docId, userId, "Hello").placeHolderMessage();
        assertThat(placeholder).extracting(MessageDto::content).isNull();
        assertThat(placeholder).extracting(MessageDto::sequenceNumber)
            .isEqualTo(123L);
    }


}
