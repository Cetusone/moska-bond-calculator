package org.example.moskacalculatorservice;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BondRequest (
        String isin,             // Идентификатор (RU000A10BK17)
        Currency currency,       // Валюта
        BigDecimal nominal,       // Номинал (напр. 1000)
        BigDecimal couponAmount,  // Размер купона (1.64)
        Integer couponPeriod,     // Период купона (30 дней)
        LocalDate maturityDate,   // Дата погашения
        BigDecimal purchasePrice, // Цена покупки (% от номинала, напр. 62.14)
        BigDecimal nkd,           // НКД (1.42)

        BigDecimal entryRate,  //текущая цена валюты
        BigDecimal targetRate    //прогнозная цена
)
{}
