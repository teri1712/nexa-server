package com.decade.nexa.messages.application.ports.out;

import com.decade.nexa.messages.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop20ByUserIdAndSequenceIdLessThanOrderBySequenceIdDesc(UUID userId, Long seq);
}
