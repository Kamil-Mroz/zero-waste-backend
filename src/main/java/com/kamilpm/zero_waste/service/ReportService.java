package com.kamilpm.zero_waste.service;

import com.kamilpm.zero_waste.domain.request.ReportRequest;

public interface ReportService {
  void createReport(ReportRequest report);

}
