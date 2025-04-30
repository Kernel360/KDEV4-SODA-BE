package com.soda.project.request.repository;

import com.soda.project.domain.stage.request.ResponseLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseLinkRepository extends JpaRepository<ResponseLink, Long> {
}
