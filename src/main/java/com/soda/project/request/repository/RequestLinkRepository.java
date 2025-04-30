package com.soda.project.request.repository;

import com.soda.project.domain.stage.request.RequestLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLinkRepository extends JpaRepository<RequestLink, Long> {
}
