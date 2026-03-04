package org.example.moskacalculatorservice;

import java.math.BigDecimal;

public record YieldCalculation(
        String isin,
        BigDecimal yieldInCurrency,     //эффективность бумаги
        BigDecimal grossYieldInRub,     //общий доход в рублях
        BigDecimal netYieldInRub,       //реальная прибыль
        BigDecimal currencyEffect       //влияние курса
) {
}
