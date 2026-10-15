package com.coinis.splitter.api.dto;

import java.util.List;

/** Clanovi su po redu ulaska; tim redom sjede za stolom. */
public record EventDetailResponse(
        long id,
        String name,
        boolean archived,
        long creatorId,
        long totalCents,
        List<MemberResponse> members,
        List<Long> pendingInviteeIds,
        List<ExpenseResponse> expenses,
        List<TransferResponse> transfers) {
}
