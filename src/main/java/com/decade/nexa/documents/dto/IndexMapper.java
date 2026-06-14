package com.decade.nexa.documents.dto;

import com.decade.nexa.documents.domain.IndexLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface IndexMapper {
    @Mapping(target = "date", source = "indexDate")
    IndexLogResponse map(IndexLog indexLog);

    List<IndexLogResponse> map(Iterable<IndexLog> indexLogs);
}
