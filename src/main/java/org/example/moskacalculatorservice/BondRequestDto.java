package org.example.moskacalculatorservice;

import java.math.BigDecimal;


public record BondRequestDto(
        String isin,
        String figi,
        Currency currency,
        BigDecimal nominal,
        BigDecimal couponAmount,
        Integer couponPeriod,
        String maturityDate,
        BigDecimal purchasePrice,
        BigDecimal nkd,

        BigDecimal entryRate,
        BigDecimal targetRate


)
{}
