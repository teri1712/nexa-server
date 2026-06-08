package com.decade.nexa.messages.application.ports.out;

import com.decade.nexa.messages.domain.AnswerMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BotMessageRepository extends JpaRepository<AnswerMessage, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE AnswerMessage m SET m.content = :content WHERE m.sequenceId = :id")
    void updateContent(Long id, String content);

    @EntityGraph(attributePaths = "userMessage")
    Optional<AnswerMessage> findByUserIdAndSequenceId(UUID userId, Long sequenceId);
}
