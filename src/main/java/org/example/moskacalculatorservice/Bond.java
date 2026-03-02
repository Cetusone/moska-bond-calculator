package org.example.moskacalculatorservice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Bond(
        String isin,
        Currency currency,
        BigDecimal nominal,
        BigDecimal couponAmount,
        Integer couponPeriod,
        LocalDate maturityDate,
        BigDecimal purchasePrice,
        BigDecimal nkd,

        BigDecimal entryRate,
        BigDecimal targetRate,
        LocalDate purchaseDate

) {
}
