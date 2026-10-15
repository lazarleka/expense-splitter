package com.coinis.splitter.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SettlementCalculatorTest {

    // Ucesnici u redu unosa: Ana=1, Marko=2, Iva=3

    @Test
    void lunchAndTaxi() {
        // Ana placa rucak 90,00; Marko placa taksi 30,00
        List<Transfer> transfers = SettlementCalculator.calculate(
                List.of(1L, 2L, 3L),
                List.of(new PaidAmount(9000, 1L), new PaidAmount(3000, 2L)));

        assertThat(transfers).containsExactlyInAnyOrder(
                new Transfer(3L, 1L, 4000),  // Iva duguje Ani 40,00
                new Transfer(2L, 1L, 1000)); // Marko duguje Ani 10,00
    }

    @Test
    void roundingRemainderGoesToFirstParticipant() {
        // Ana placa 100,00 za troje: udjeli 33,34 + 33,33 + 33,33
        List<Transfer> transfers = SettlementCalculator.calculate(
                List.of(1L, 2L, 3L),
                List.of(new PaidAmount(10000, 1L)));

        assertThat(transfers).containsExactlyInAnyOrder(
                new Transfer(2L, 1L, 3333),  // Marko duguje Ani 33,33
                new Transfer(3L, 1L, 3333)); // Iva duguje Ani 33,33
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
}
