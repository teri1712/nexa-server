package com.decade.nexa.collections.domain;

import com.decade.nexa.collections.application.ports.out.CollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionAccessPolicy {
    final CollectionRepository collections;

    public void apply(UUID userId, Long collectionId) {
        Collection collection = collections.findById(collectionId).orElseThrow(
            () -> new AccessDeniedException("You don't have access to this collection")
        );
        if (!collection.userId().equals(userId)) {
            throw new AccessDeniedException("You don't have access to this collection");
        }
    }
}
