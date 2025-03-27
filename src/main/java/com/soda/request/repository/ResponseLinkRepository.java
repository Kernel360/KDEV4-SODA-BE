package com.soda.request.repository;

import com.soda.request.entity.ResponseLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseLinkRepository extends JpaRepository<ResponseLink, Long> {
}
