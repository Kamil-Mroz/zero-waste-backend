package com.kamilpm.zero_waste.service;

import java.util.List;
import java.util.UUID;


import com.kamilpm.zero_waste.domain.dto.ReportDto;
import com.kamilpm.zero_waste.domain.request.ReportRequest;
import com.kamilpm.zero_waste.domain.request.ResolveReportRequest;

public interface ReportService {
  void createReport(ReportRequest report);

  List<ReportDto> getReports();

  void rejectReport(UUID reportId, String adminNote);

  void resolveReport(UUID reportId, ResolveReportRequest resolveRequest) ;
  void rejectAllBySubjectId(UUID subjectId);

}
