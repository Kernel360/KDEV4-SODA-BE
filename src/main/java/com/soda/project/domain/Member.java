package com.soda.project.domain;

import com.soda.project.domain.stage.request.Request;

import java.util.Collection;

public record Member(Long id, Collection<Request> requests) {
}
