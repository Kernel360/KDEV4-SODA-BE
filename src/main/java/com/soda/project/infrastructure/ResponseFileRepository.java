package com.soda.project.infrastructure;

import com.soda.project.domain.stage.request.ResponseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseFileRepository extends JpaRepository<ResponseFile, Long> {

}
