package com.soda.project.infrastructure;

import com.soda.project.domain.stage.request.file.RequestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestFileRepository extends JpaRepository<RequestFile, Long> {
}
