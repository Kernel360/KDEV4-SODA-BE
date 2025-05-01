package com.soda.project.domain.event;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProjectCreatedEvent {
    private final LocalDate creationDate;

    public ProjectCreatedEvent (LocalDate creationDate) {
        this.creationDate = creationDate;
    }

}
