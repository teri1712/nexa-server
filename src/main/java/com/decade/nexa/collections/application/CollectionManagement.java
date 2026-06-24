package com.decade.nexa.collections.application;

import com.decade.nexa.collections.application.ports.out.CollectionItemRepository;
import com.decade.nexa.documents.domain.events.DocDeleted;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CollectionManagement {

    final CollectionItemRepository items;

    @Async
    @EventListener
    public void on(DocDeleted event) {
        items.deleteBydocumentId(event.id());
    }
}
