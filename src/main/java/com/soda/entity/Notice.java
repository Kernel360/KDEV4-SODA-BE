package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Notice extends BaseEntity {

    private String title;

    private String content;

    private noticeType noticeType;

    private Long relatedEntityId;

    @Column(nullable = false)
    private Boolean isChecked;

    @PrePersist
    public void prePersistNotice() {
        if (this.isChecked == null) {
            this.isChecked = false;
        }
    }
}
