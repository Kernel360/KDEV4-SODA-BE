package com.soda.project.request.repository;

import com.soda.project.domain.stage.request.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, RequestRepositoryCustom {
    List<Request> findAllByStage_IdAndIsDeletedFalse(Long stageId);
}
