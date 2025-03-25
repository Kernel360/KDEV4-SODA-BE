package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.request.entity.Request;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseEntity {

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Request> requestList = new ArrayList<>();

    @Builder
    public Task(String title, String content, Stage stage) {
        this.title = title;
        this.content = content;
        this.stage = stage;
    }
}
