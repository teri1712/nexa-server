package com.decade.nexa.messages.unit;

import com.decade.nexa.messages.application.ports.out.Agent;
import com.decade.nexa.messages.application.ports.out.AnswerMessageRepository;
import com.decade.nexa.messages.application.services.FulfillmentServiceImpl;
import com.decade.nexa.messages.domain.AnswerMessage;
import com.decade.nexa.messages.domain.UserMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FulfillmentServiceTest {

    @Mock
    AnswerMessageRepository answers;

    @Mock
    Agent agent;

    @InjectMocks
    FulfillmentServiceImpl fulfillmentService;

    @Test
    void shouldFindMatchedAnswerMessageUpdateItsMessage() {
        String docId = "123";
        UUID userId = UUID.randomUUID();
        Long placeholderSequence = 123L;

        UserMessage userMessage = new UserMessage(docId, userId, "What is Kubernetes?");
        AnswerMessage answerMessage = new AnswerMessage(userMessage, userId, docId);

        when(answers.findByUserIdAndSequenceId(eq(userId), eq(placeholderSequence)))
            .thenReturn(Optional.of(answerMessage));
        when(agent.generate(eq(docId), eq(userId), eq("What is Kubernetes?")))
            .thenReturn(Flux.just("Kubernetes is a container ", "orchestration system"));

        fulfillmentService.fill(userId, placeholderSequence).subscribe();

        verify(answers).updateContent(eq(placeholderSequence), eq("Kubernetes is a container orchestration system"));
    }
}
