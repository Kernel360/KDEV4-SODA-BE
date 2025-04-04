package com.soda.project.stage.task;

import com.soda.common.BaseEntity;
import com.soda.project.stage.Stage;
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

    @Column(nullable = false)
    private Float taskOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Request> requestList = new ArrayList<>();

    @Builder
    public Task(String title, String content, Float taskOrder, Stage stage) {
        this.taskOrder = taskOrder;
        this.title = title;
        this.content = content;
        this.stage = stage;
    }

    public void update(String newTitle, String newContent) {
        if (newTitle != null) {
            this.title = newTitle;
        }
        if (newContent != null) {
            this.content = newContent;
        }
    }

    public void moveTaskOrder(Float newTaskOrder) {
        this.taskOrder = newTaskOrder;
    }

    public void delete() {
        this.markAsDeleted();
    }
}
