package com.decade.nexa.collections.application.services;

import com.decade.nexa.collections.application.ports.out.CollectionRepository;
import com.decade.nexa.collections.domain.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    final CollectionRepository collections;

    public Collection create(UUID user, String name, Long parentId) {
        return collections.save(new Collection(null, user, name, parentId));
    }

    public List<Collection> list(UUID user) {
        return collections.findByUserId(user);
    }

}
