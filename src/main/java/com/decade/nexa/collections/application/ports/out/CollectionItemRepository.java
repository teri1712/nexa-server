package com.decade.nexa.collections.application.ports.out;

import com.decade.nexa.collections.domain.CollectionItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionItemRepository extends CrudRepository<CollectionItem, Long> {
    List<CollectionItem> findByCollectionId(Long collectionId);

    void deleteBydocumentId(String documentId);
}

