package com.kamilpm.zero_waste.moderation.mapper;

import org.springframework.stereotype.Component;

import com.kamilpm.zero_waste.moderation.dto.ReportDto;
import com.kamilpm.zero_waste.moderation.entity.Report;
import com.kamilpm.zero_waste.moderation.dto.UserSummaryDto;

@Component
public class ReportMapper {
  public ReportDto toDto(Report report, UserSummaryDto reporter, UserSummaryDto resolvedBy) {

    if (report == null) {
      return null;
    }

    return new ReportDto(report.getId(),
        reporter,
        report.getSubjectType(),
        report.getSubjectId(),
        report.getReason(),
        report.getComment(),
        report.getStatus(),
        report.getResolvedAt(),
        resolvedBy,
        report.getAdminNote());
  }
}
