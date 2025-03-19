package com.soda.entity;

import com.soda.entity.enums.NoticeType;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Notice extends BaseEntity {

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private NoticeType noticeType;

    private Long relatedEntityId;

    @Column(nullable = false)
    private Boolean isChecked;

    @PostLoad
    private void prePersistNotice() {
        if (this.isChecked == null) {
            this.isChecked = false;
        }
    }
}
