package com.soda.project.domain.stage.common.link;

import com.soda.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LinkBase extends BaseEntity {

    @Column(nullable = false)
    protected String urlAddress;

    protected String urlDescription;

    public void delete() {
        markAsDeleted();
    }

    public abstract Long getDomainId();
}