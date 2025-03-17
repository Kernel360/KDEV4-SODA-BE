package com.soda.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class StageTask extends BaseEntity{

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_stage_id", nullable = false)
    private ProjectStage projectStage;

    @OneToMany(mappedBy = "stageTask", cascade = CascadeType.ALL)
    private List<TaskFile> taskFileList = new ArrayList<>();

    @OneToMany(mappedBy = "stageTask", cascade = CascadeType.ALL)
    private List<TaskLink> taskLinkList = new ArrayList<>();
}
