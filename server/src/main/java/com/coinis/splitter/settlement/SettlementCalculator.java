package com.coinis.splitter.settlement;

import java.util.ArrayList;
import java.util.List;

/**
 * Racuna ko kome duguje. Svi iznosi su u centima (long), bez float aritmetike.
 */
public final class SettlementCalculator {

    private SettlementCalculator() {
    }

    /**
     * @param participantIds id-jevi ucesnika u redu unosa
     * @param expenses       svi troskovi grupe
     * @return dugovanja; prazna lista ako nema troskova
     */
    public static List<Transfer> calculate(List<Long> participantIds, List<PaidAmount> expenses) {
        int n = participantIds.size();
        long[] balances = new long[n];

        for (PaidAmount expense : expenses) {
            int payerIndex = participantIds.indexOf(expense.payerId());
            if (payerIndex < 0) {
                throw new IllegalArgumentException("Platilac nije ucesnik grupe: " + expense.payerId());
            }
            balances[payerIndex] += expense.amountCents();

            long[] shares = shares(expense.amountCents(), n);
            for (int i = 0; i < n; i++) {
                balances[i] -= shares[i];
            }
        }

        return settle(participantIds, balances);
    }

    /**
     * Dijeli iznos na n udjela. Visak centa ide prvima po redu.
     * Npr. 10000 na 3 ucesnika daje 3334, 3333, 3333.
     */
    public static long[] shares(long amountCents, int n) {
        long base = amountCents / n;
        long remainder = amountCents % n;

        long[] shares = new long[n];
        for (int i = 0; i < n; i++) {
            shares[i] = base + (i < remainder ? 1 : 0);
        }
        return shares;
    }

    // Duznike i povjerioce uparuje po redu unosa, dok sva stanja ne budu nula.
    private static List<Transfer> settle(List<Long> participantIds, long[] balances) {
        List<Transfer> transfers = new ArrayList<>();
        int creditor = 0;
        int debtor = 0;
        int n = balances.length;

        while (true) {
            while (creditor < n && balances[creditor] <= 0) {
                creditor++;
            }
            while (debtor < n && balances[debtor] >= 0) {
                debtor++;
            }
            if (creditor >= n || debtor >= n) {
                break;
            }

            long amount = Math.min(balances[creditor], -balances[debtor]);
            transfers.add(new Transfer(participantIds.get(debtor), participantIds.get(creditor), amount));
            balances[creditor] -= amount;
            balances[debtor] += amount;
        }

        return transfers;
    }
}
