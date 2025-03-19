package com.soda.entity;

import com.soda.entity.enums.LinkType;
import jakarta.persistence.Entity;
import lombok.Getter;

@Entity
@Getter
public class Link extends BaseEntity {
    private String urlAddress;

    private String urlDescription;

    private LinkType type;

    private Long relatedEntityId;
}