package com.kamilpm.zero_waste.common.dto;

import java.util.List;

public record CursorResponse<T>(List<T> items, CursorRequest nextCursor, boolean hasMore, CursorRequest prevCursor,
    boolean hasPrev) {

}
