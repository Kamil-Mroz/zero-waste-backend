package com.kamilpm.zero_waste.common.events;

import java.util.List;

public record SendBansNotificationEvent(List<String> usersEmail) {

}
