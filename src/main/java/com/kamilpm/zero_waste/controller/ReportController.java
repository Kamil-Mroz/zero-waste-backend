package com.kamilpm.zero_waste.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kamilpm.zero_waste.domain.dto.ReportDto;
import com.kamilpm.zero_waste.domain.request.ReportRejectRequest;
import com.kamilpm.zero_waste.domain.request.ReportRequest;
import com.kamilpm.zero_waste.domain.request.ResolveReportRequest;
import com.kamilpm.zero_waste.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(path = "/api/v{version}/reports", version = "1")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @PostMapping
  public ResponseEntity<Void> createReport(@Valid @RequestBody ReportRequest report) {

    reportService.createReport(report);

    return ResponseEntity.ok().build();

  }

  @GetMapping
  public ResponseEntity<List<ReportDto>> getReports() {

    return ResponseEntity.ok(
        reportService.getReports());
  }

  @PostMapping("{id}/reject")
  public ResponseEntity<Void> rejectReport(@PathVariable UUID id,
      @Valid @RequestBody ReportRejectRequest rejectRequest) {
    reportService.rejectReport(id, rejectRequest.adminNote());
    return ResponseEntity.ok().build();
  }

  @PostMapping("{id}/resolve")
  public ResponseEntity<Void> resolveReport(@PathVariable UUID id,
      @Valid @RequestBody ResolveReportRequest resolveRequest) {
    reportService.resolveReport(id, resolveRequest);
    return ResponseEntity.ok().build();
  }

}
