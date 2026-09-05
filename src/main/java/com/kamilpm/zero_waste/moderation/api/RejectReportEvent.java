package com.kamilpm.zero_waste.moderation.api;

import java.util.UUID;

public record RejectReportEvent(UUID subjectId, boolean isAdmin) {

}
