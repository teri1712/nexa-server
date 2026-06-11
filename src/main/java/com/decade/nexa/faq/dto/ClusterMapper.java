package com.decade.nexa.faq.dto;


import com.decade.nexa.faq.domain.ClusterLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClusterMapper {
    @Mapping(target = "date", source = "clusterDate")
    ClusterLogResponse map(ClusterLog clusterLog);

    List<ClusterLogResponse> map(Iterable<ClusterLog> clusterLogs);
}
