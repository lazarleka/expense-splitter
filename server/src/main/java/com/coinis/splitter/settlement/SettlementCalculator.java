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
     * @param memberIds id-jevi clanova po redu ulaska
     * @param expenses  svi troskovi dogadjaja
     * @return dugovanja; prazna lista ako nema troskova
     */
    public static List<Transfer> calculate(List<Long> memberIds, List<PaidAmount> expenses) {
        return settle(memberIds, balances(memberIds, expenses));
    }

    /**
     * Stanje svakog clana (istim redom kao memberIds): sta je platio minus zbir njegovih udjela.
     * Pozitivno: potrazuje; negativno: duguje.
     */
    public static long[] balances(List<Long> memberIds, List<PaidAmount> expenses) {
        long[] balances = new long[memberIds.size()];

        for (PaidAmount expense : expenses) {
            int sharedBy = expense.memberCount();
            if (sharedBy < 1 || sharedBy > memberIds.size()) {
                throw new IllegalArgumentException("Neispravan broj clanova troska: " + sharedBy);
            }
            int payerIndex = memberIds.indexOf(expense.payerId());
            if (payerIndex < 0 || payerIndex >= sharedBy) {
                throw new IllegalArgumentException("Platilac nije bio clan u trenutku troska: " + expense.payerId());
            }
            balances[payerIndex] += expense.amountCents();

            long[] shares = shares(expense.amountCents(), sharedBy);
            for (int i = 0; i < sharedBy; i++) {
                balances[i] -= shares[i];
            }
        }

        return balances;
    }

    /**
     * Dijeli iznos na n udjela. Visak centa ide prvima po redu.
     * Npr. 10000 na 3 clana daje 3334, 3333, 3333.
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

    // Duznike i povjerioce uparuje po redu ulaska, dok sva stanja ne budu nula.
    private static List<Transfer> settle(List<Long> memberIds, long[] balances) {
        long[] remaining = balances.clone();
        List<Transfer> transfers = new ArrayList<>();
        int creditor = 0;
        int debtor = 0;
        int n = remaining.length;

        while (true) {
            while (creditor < n && remaining[creditor] <= 0) {
                creditor++;
            }
            while (debtor < n && remaining[debtor] >= 0) {
                debtor++;
            }
            if (creditor >= n || debtor >= n) {
                break;
            }

            long amount = Math.min(remaining[creditor], -remaining[debtor]);
            transfers.add(new Transfer(memberIds.get(debtor), memberIds.get(creditor), amount));
            remaining[creditor] -= amount;
            remaining[debtor] += amount;
        }

        return transfers;
    }
}
