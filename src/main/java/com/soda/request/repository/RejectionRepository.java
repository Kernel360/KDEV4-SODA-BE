package com.soda.request.repository;

import com.soda.request.entity.Rejection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RejectionRepository extends JpaRepository<Rejection, Long> {
}
