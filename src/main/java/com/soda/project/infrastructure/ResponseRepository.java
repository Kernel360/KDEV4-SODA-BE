package com.soda.project.infrastructure;

import com.soda.project.domain.stage.request.response.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    List<Response> findAllByRequest_IdAndIsDeletedFalse(Long StageId);

    @Query("SELECT COUNT(r) FROM Response r WHERE r.request.id = :requestId AND r.isDeleted = false")
    long countNotDeletedByRequestId(@Param("requestId") Long requestId);
}
