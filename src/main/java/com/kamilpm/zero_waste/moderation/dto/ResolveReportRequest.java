package com.kamilpm.zero_waste.moderation.dto;



import com.kamilpm.zero_waste.moderation.entity.ReportAction;

import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;

public record ResolveReportRequest(
    @NonNull  ReportAction action,
    @NonNull  @NotEmpty String adminNote) {

}
