package com.coinis.splitter.settlement;

/**
 * Trosak za obracun: iznos u centima i id ucesnika koji je platio.
 * Trosak se uvijek dijeli jednako na sve ucesnike grupe.
 */
public record Expense(long amountCents, long payerId) {
}
