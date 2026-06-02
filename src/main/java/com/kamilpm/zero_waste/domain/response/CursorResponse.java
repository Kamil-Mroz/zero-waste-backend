package com.kamilpm.zero_waste.domain.response;

import java.util.List;

import com.kamilpm.zero_waste.domain.request.CursorRequest;

public record CursorResponse<T>(List<T> items, CursorRequest nextCursor, boolean hasMore, CursorRequest prevCursor,
    boolean hasPrev) {

}
