package com.kamilpm.zero_waste.notification.api;

public record SendReportNotificationEvent(NotificationReferenceType subjectType, String comment) {

}
