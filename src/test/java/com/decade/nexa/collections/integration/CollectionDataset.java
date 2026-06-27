package com.decade.nexa.collections.integration;

import com.decade.nexa.collections.application.ports.out.CollectionItemRepository;
import com.decade.nexa.collections.application.ports.out.CollectionRepository;
import com.decade.nexa.collections.domain.Collection;
import com.decade.nexa.common.TestDataset;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;

import java.util.UUID;

@RequiredArgsConstructor
@TestComponent
public class CollectionDataset implements TestDataset {

    private final CollectionRepository collections;
    private final CollectionItemRepository items;

    @Override
    public void setup() {
        try {
            UUID bobId = UUID.fromString("22222222-2222-2222-2222-222222222222");
            collections.save(new Collection(null, bobId, "Bob's Secrets", null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void clean() {
        items.deleteAll();
        collections.deleteAll();
    }

}

