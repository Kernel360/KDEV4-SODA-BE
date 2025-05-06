package com.soda.project.infrastructure.stage.request.response;

import com.soda.project.domain.stage.request.response.link.ResponseLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseLinkRepository extends JpaRepository<ResponseLink, Long> {
}
