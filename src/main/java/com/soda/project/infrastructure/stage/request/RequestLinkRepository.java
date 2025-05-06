package com.soda.project.infrastructure.stage.request;

import com.soda.project.domain.stage.request.link.RequestLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLinkRepository extends JpaRepository<RequestLink, Long> {
}
