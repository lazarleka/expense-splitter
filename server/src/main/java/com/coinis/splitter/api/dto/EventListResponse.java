package com.coinis.splitter.api.dto;

import java.util.List;

public record EventListResponse(List<EventSummaryResponse> active, List<EventSummaryResponse> archived) {
}
