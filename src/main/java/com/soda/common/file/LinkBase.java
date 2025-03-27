package com.soda.common.file;

import com.soda.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class LinkBase extends BaseEntity {

    @Column(nullable = false)
    protected String urlAddress;

    protected String urlDescription;

    public LinkBase(String urlAddress, String urlDescription) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
    }
}