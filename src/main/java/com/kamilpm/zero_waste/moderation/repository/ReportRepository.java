package com.kamilpm.zero_waste.moderation.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamilpm.zero_waste.moderation.entity.Report;
import com.kamilpm.zero_waste.moderation.entity.ReportStatus;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

  Optional<Report> findById(UUID reportId);

  boolean existsByReporterIdAndSubjectId(UUID reporterId, UUID subjectId);

  List<Report> findAllByOrderByCreatedAtDesc();

  List<Report> findBySubjectIdAndStatus(UUID id, ReportStatus status);
}
