package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Notice extends BaseEntity {

    private String title;

    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private StageTask stageTask;

    @Column(nullable = false)
    private Boolean isChecked;

    @PrePersist
    public void prePersistNotice() {
        if (this.isChecked == null) {
            this.isChecked = false;
        }
    }
}
