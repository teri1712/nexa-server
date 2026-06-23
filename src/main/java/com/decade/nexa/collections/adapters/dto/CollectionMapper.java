package com.decade.nexa.collections.adapters.dto;


import com.decade.nexa.collections.domain.Collection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CollectionMapper {
    CollectionResponse map(Collection collection);

    List<CollectionResponse> map(Iterable<Collection> collections);
}
