package com.soda.request.repository;

import com.soda.request.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByStage_IdAndIsDeletedFalse(Long stageId);
}
