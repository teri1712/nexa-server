package com.decade.nexa.messages.dto;

import com.decade.nexa.messages.domain.Message;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageMapper {

    MessageResponse toDto(Message message);

}