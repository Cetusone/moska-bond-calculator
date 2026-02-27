package org.example.moskacalculatorservice;

import java.math.BigDecimal;

public record BondResponse (
    String isin,             // Идентификатор (RU000A10BK17)
    BigDecimal couponYield,         //купонная доходность
    BigDecimal currentYield,        //текущая доходность
    BigDecimal simpleYearlyYieldRub,//доходность при девальвации через год
    BigDecimal ytmRub               //доходность при девальвации к погашению
){}
