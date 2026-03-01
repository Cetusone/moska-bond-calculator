package org.example.moskacalculatorservice;

import java.math.BigDecimal;

public record BondResponse(
        String isin,
        BigDecimal yieldInCurrency,
        BigDecimal grossYieldInRub,
        BigDecimal netYieldInRub,
        BigDecimal netProfitRub,
        BigDecimal currencyEffectPercent) {
}
