package com.kamilpm.zero_waste.common.events;

import java.util.UUID;

public record RejectReportEvent(UUID subjectId, boolean isAdmin) {

}
