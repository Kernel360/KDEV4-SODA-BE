package com.soda.request.repository;

import com.soda.request.entity.ResponseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseFileRepository extends JpaRepository<ResponseFile, Long> {

}
