package com.kamilpm.zero_waste.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

  @EntityGraph(attributePaths = { "reporter" })
  boolean existsByReporter_IdAndSubjectId(UUID reporterId, UUID subjectId);

}
