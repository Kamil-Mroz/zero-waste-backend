package com.kamilpm.zero_waste.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.domain.entity.Report;
import com.kamilpm.zero_waste.domain.entity.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

  @EntityGraph(attributePaths = { "reporter" })
  Optional<Report> findById(UUID reportId);

  @EntityGraph(attributePaths = { "reporter" })
  boolean existsByReporter_IdAndSubjectId(UUID reporterId, UUID subjectId);

  @EntityGraph(attributePaths = { "reporter", "resolvedBy" })
  List<Report> findAllByOrderByCreatedAtDesc();

  List<Report> findBySubjectIdAndStatus(UUID id, ReportStatus status);
}
