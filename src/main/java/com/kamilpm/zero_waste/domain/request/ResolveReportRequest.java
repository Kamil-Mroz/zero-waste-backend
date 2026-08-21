package com.kamilpm.zero_waste.domain.request;

import com.kamilpm.zero_waste.domain.entity.ReportAction;

public record ResolveReportRequest(
    ReportAction action,
    String adminNote) {

}
