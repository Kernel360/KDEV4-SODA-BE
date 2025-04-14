package com.soda.request.repository;

import com.soda.request.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findAllByRequest_IdAndIsDeletedFalse(Long StageId);
}
