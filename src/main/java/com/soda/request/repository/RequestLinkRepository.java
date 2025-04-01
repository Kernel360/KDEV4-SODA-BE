package com.soda.request.repository;

import com.soda.request.entity.RequestLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestLinkRepository extends JpaRepository<RequestLink, Long> {
}
