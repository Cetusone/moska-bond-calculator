package org.example.moskacalculatorservice;

import java.math.BigDecimal;

public record BondRequest (
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


)
{}
