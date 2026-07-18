package com.nitax.valueplusbackend.dto.mapper;

import com.nitax.valueplusbackend.domain.Publisher;
import com.nitax.valueplusbackend.dto.response.PublisherResponse;

public class EntityDtoMapper {
    public static PublisherResponse toResponse(Publisher entity) {
        PublisherResponse response = new PublisherResponse();
        response.setIdentifier(entity.getIdentifier());
        response.setName(entity.getName());
        response.setEmail(entity.getEmail());
        response.setIdentifier(entity.getIdentifier());
        response.setPubId(entity.getPubId());
        response.setWebsite(entity.getWebsite());
        response.setPubUrl(entity.getPubUrl());
        response.setPostbackUrl(entity.getPostbackUrl());
        response.setClickIdParameter(entity.getClickIdParameter());
        response.setSourceIdParameter(entity.getSourceIdParameter());
        response.setTotalDueAmount(entity.getTotalDueAmount());
        response.setApiKey(entity.getApiKey());

        response.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : "");
        response.setCreatedAt(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : "");
        response.setModifiedAt(entity.getLastModifiedDate() != null ? entity.getLastModifiedDate().toString() : "");
        return response;
    }
}
