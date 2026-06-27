package com.decade.nexa.collections.application.ports.out;

import com.decade.nexa.collections.domain.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CollectionRepository extends CrudRepository<Collection, Long> {
    List<Collection> findByUserId(UUID userId);

    List<Collection> findByParentId(Long parentId);
}

