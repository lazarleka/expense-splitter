package com.coinis.splitter.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class SettlementCalculatorTest {

    // Clanovi po redu ulaska: Ana=1, Marko=2, Iva=3
    private static final List<Long> ANA_MARKO_IVA = List.of(1L, 2L, 3L);

    @Test
    void lunchAndTaxi() {
        // Ana placa rucak 90,00; Marko placa taksi 30,00; svi troje su clanovi
        List<Transfer> transfers = SettlementCalculator.calculate(ANA_MARKO_IVA,
                List.of(new PaidAmount(9000, 1L, 3), new PaidAmount(3000, 2L, 3)));

        assertThat(transfers).containsExactly(
                new Transfer(2L, 1L, 1000),  // Marko duguje Ani 10,00
                new Transfer(3L, 1L, 4000)); // Iva duguje Ani 40,00
    }

    @Test
    void roundingRemainderGoesToFirstMember() {
        // Ana placa 100,00 za troje: udjeli 33,34 + 33,33 + 33,33
        List<Transfer> transfers = SettlementCalculator.calculate(ANA_MARKO_IVA,
                List.of(new PaidAmount(10000, 1L, 3)));

        assertThat(transfers).containsExactly(
                new Transfer(2L, 1L, 3333),  // Marko duguje Ani 33,33
                new Transfer(3L, 1L, 3333)); // Iva duguje Ani 33,33
    }

    @Test
    void laterMemberDoesNotShareEarlierExpenses() {
        // Ana placa 60,00 dok su clanovi Ana i Marko; Iva ulazi; Marko placa 30,00 za troje
        List<PaidAmount> expenses = List.of(new PaidAmount(6000, 1L, 2), new PaidAmount(3000, 2L, 3));

        assertThat(SettlementCalculator.balances(ANA_MARKO_IVA, expenses))
                .containsExactly(2000, -1000, -1000);
        assertThat(SettlementCalculator.calculate(ANA_MARKO_IVA, expenses)).containsExactly(
                new Transfer(2L, 1L, 1000),  // Marko duguje Ani 10,00
                new Transfer(3L, 1L, 1000)); // Iva duguje Ani 10,00
    }

    @Test
    void balancesSumToZero() {
        long[] balances = SettlementCalculator.balances(ANA_MARKO_IVA,
                List.of(new PaidAmount(10000, 1L, 3), new PaidAmount(701, 3L, 3)));

        assertThat(balances[0] + balances[1] + balances[2]).isZero();
    }

    @Test
    void sharesSumToExactAmount() {
        long[] shares = SettlementCalculator.shares(10000, 3);

        assertThat(shares).containsExactly(3334, 3333, 3333);
        assertThat(shares[0] + shares[1] + shares[2]).isEqualTo(10000);
    }

    @Test
    void noExpensesMeansNoTransfers() {
        assertThat(SettlementCalculator.calculate(List.of(1L, 2L), List.of())).isEmpty();
    }

    @Test
    void payerMustBeMemberAtTimeOfExpense() {
        // Iva (treca po redu) ne moze platiti trosak koji dijele samo prva dva clana
        assertThatThrownBy(() -> SettlementCalculator.balances(ANA_MARKO_IVA, List.of(new PaidAmount(1000, 3L, 2))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
