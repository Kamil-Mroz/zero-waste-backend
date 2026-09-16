package com.kamilpm.zero_waste.common.events;

import com.kamilpm.zero_waste.common.dto.NotificationReferenceType;

public record SendReportNotificationEvent(NotificationReferenceType subjectType, String comment) {

}
