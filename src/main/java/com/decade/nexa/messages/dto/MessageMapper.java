package com.decade.nexa.messages.dto;

import com.decade.nexa.messages.domain.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageMapper {

    @Mapping(target = "content", source = "content")
    @Mapping(target = "sequenceNumber", source = "sequenceId")
    @Mapping(target = "mine", expression = "java(message instanceof com.decade.nexa.messages.domain.UserMessage)")
    MessageDto toDto(Message message);

}