package org.example.moskacalculatorservice.api;

import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

public class TInvestMapper {

    public static BigDecimal toBigDecimal(Quotation quotation) {
        if (quotation == null) {
            return BigDecimal.ZERO;
        }
        return quotation.getUnits() == 0 && quotation.getNano() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(quotation.getUnits())
                .add(BigDecimal.valueOf(quotation.getNano(), 9));
    }

    public static BigDecimal toBigDecimal(MoneyValue moneyValue) {
        if (moneyValue == null) {
            return BigDecimal.ZERO;
        }
        return moneyValue.getUnits() == 0 && moneyValue.getNano() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(moneyValue.getUnits())
                .add(BigDecimal.valueOf(moneyValue.getNano(), 9));
    }
}