package com.decade.nexa.collections.application.services;

import com.decade.nexa.collections.application.ports.out.CollectionItemRepository;
import com.decade.nexa.collections.application.ports.out.CollectionRepository;
import com.decade.nexa.collections.domain.Collection;
import com.decade.nexa.collections.domain.CollectionAccessPolicy;
import com.decade.nexa.collections.domain.CollectionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionItemService {

    final CollectionItemRepository items;
    final CollectionRepository collections;
    final CollectionAccessPolicy policy;

    public void create(UUID userId, Long collectionId, String docId) {
        policy.apply(userId, collectionId);
        items.save(new CollectionItem(null, collectionId, docId, LocalDate.now()));
    }

    public List<CollectionItem> list(UUID user, Long collectionIds) {
        policy.apply(user, collectionIds);
        return items.findByCollectionId(collectionIds);
    }

    public List<Collection> listCollections(UUID user, Long parentId) {
        policy.apply(user, parentId);
        return collections.findByParentId(parentId);
    }
}
