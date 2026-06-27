package com.decade.nexa.collections.adapters;

import com.decade.nexa.collections.adapters.dto.CollectionItemResponse;
import com.decade.nexa.collections.adapters.dto.CollectionMapper;
import com.decade.nexa.collections.adapters.dto.CollectionResponse;
import com.decade.nexa.collections.application.services.CollectionItemService;
import com.decade.nexa.collections.application.services.CollectionService;
import com.decade.nexa.collections.domain.CollectionItem;
import com.decade.nexa.common.security.UserId;
import com.decade.nexa.documents.api.DocInfo;
import com.decade.nexa.documents.api.DocumentApi;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "bearerAuth")
public class CollectionController {
    final CollectionService collectionService;
    final CollectionItemService collectionItemService;
    final CollectionMapper mapper;
    final DocumentApi documentApi;

    @GetMapping
    List<CollectionResponse> list(@UserId UUID userId) {
        return mapper.map(collectionService.list(userId));
    }

    @PostMapping
    CollectionResponse create(@UserId UUID userId, @RequestParam String name, @RequestParam(required = false) Long parentId) {
        return mapper.map(collectionService.create(userId, name, parentId));
    }

    @GetMapping("/{collectionId}/items")
    List<CollectionItemResponse> list(@PathVariable Long collectionId, @UserId UUID userId) {
        List<CollectionItem> items = collectionItemService.list(userId, collectionId);
        if (items.isEmpty()) {
            return List.of();
        }
        Set<String> docIds = items.stream()
            .map(CollectionItem::documentId)
            .collect(Collectors.toSet());
        Map<String, DocInfo> docInfos = documentApi.find(docIds);
        return items.stream()
            .map(item -> {
                DocInfo info = docInfos.get(item.documentId());
                String title = info != null ? info.title() : null;
                String filename = info != null ? info.filename() : null;
                return new CollectionItemResponse(item.documentId(), title, filename, item.addedAt());
            })
            .toList();
    }

    @GetMapping("/{parentId}/collections")
    List<CollectionResponse> listCollections(@PathVariable Long parentId, @UserId UUID userId) {
        return mapper.map(collectionItemService.listCollections(userId, parentId));
    }

    @PostMapping("/{collectionId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    void addItem(@PathVariable Long collectionId, @RequestParam String docId, @UserId UUID userId) {
        collectionItemService.create(userId, collectionId, docId);
    }

}

