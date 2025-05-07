package com.soda.project.infrastructure.stage.request.response.file;

import com.soda.project.domain.stage.request.response.file.ResponseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseFileRepository extends JpaRepository<ResponseFile, Long> {

}
