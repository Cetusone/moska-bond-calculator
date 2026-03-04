package org.example.moskacalculatorservice.api;

import org.example.moskacalculatorservice.Currency;

import java.math.BigDecimal;

public record TDataDto(
        String isin,
        Currency currency,
        BigDecimal nominal,
        BigDecimal couponAmount,
        Integer couponPeriod,
        String maturityDate,
        BigDecimal purchasePrice,
        BigDecimal nkd,

        BigDecimal entryRate,
        BigDecimal targetRate
) {
}
