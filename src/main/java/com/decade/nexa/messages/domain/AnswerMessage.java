package com.decade.nexa.messages.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor
@Table(name = "answer_messages")
@Getter
public class AnswerMessage extends Message {

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_message_id", nullable = false)
    private UserMessage userMessage;

    public AnswerMessage(UserMessage userMessage, UUID userId, String docId) {
        super(null, userId, docId);
        this.userMessage = userMessage;
    }

    public void setContent(String content) {
        super.setContent(content);
    }
}
