package com.soda.project.entity;

import com.soda.common.BaseEntity;
import com.soda.request.entity.Request;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Task extends BaseEntity {

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Request> requestList = new ArrayList<>();
}
