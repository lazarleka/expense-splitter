package com.coinis.splitter.api.dto;

public record ExpenseResponse(Long id, String title, long amountCents, Long payerId, String payerName) {
}
