package com.decade.nexa.messages.application.ports.out;

import com.decade.nexa.messages.domain.AgentMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AgentMessageRepository extends JpaRepository<AgentMessage, Long> {


    @Transactional
    @Modifying
    @Query("UPDATE AgentMessage m SET m.content = :content WHERE m.sequenceId = :id")
    void updateContent(Long id, String content);

    @EntityGraph(attributePaths = "userMessage")
    Optional<AgentMessage> findByUserIdAndSequenceId(UUID userId, Long sequenceId);
}
