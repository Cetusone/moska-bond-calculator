package org.example.moskacalculatorservice;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class BondCalculatorEngine {
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.13"); // НДФЛ 13%



    public YieldCalculation simpleCalculate(Bond bond) {
        long days = calculateDaysToMaturity(bond.purchaseDate(), bond.maturityDate());
        BigDecimal annualMultiplier = DAYS_IN_YEAR.divide(BigDecimal.valueOf(days), MC);

        BigDecimal investedRub = calculateInvestedRub(bond);

        BigDecimal totalPayoutCurr = calculateTotalPayoutCurrency(bond, days);
        BigDecimal totalPayoutRub = totalPayoutCurr.multiply(bond.targetRate(), MC);

        BigDecimal netProfitRub = calculateNetProfitRub(investedRub, totalPayoutRub);

        return new YieldCalculation(
                bond.isin(),
                calculateSimpleYield(totalPayoutCurr.divide(calculateDirtyPrice(bond), MC).subtract(BigDecimal.ONE), annualMultiplier),
                calculateSimpleYield(totalPayoutRub.subtract(investedRub).divide(investedRub, MC), annualMultiplier),
                calculateSimpleYield(netProfitRub.divide(investedRub, MC), annualMultiplier),
                calculateCurrencyEffect(bond.entryRate(), bond.targetRate(), annualMultiplier)
        );
    }


    private long calculateDaysToMaturity(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0) throw new IllegalArgumentException("Invalid dates");
        return days;
    }

    private BigDecimal calculateDirtyPrice(Bond bond) {
        BigDecimal priceRatio = bond.purchasePrice().divide(HUNDRED, MC);
        return bond.nominal().multiply(priceRatio, MC).add(bond.nkd(), MC);
    }

    private BigDecimal calculateInvestedRub(Bond bond) {
        return calculateDirtyPrice(bond).multiply(bond.entryRate(), MC);
    }

    private BigDecimal calculateTotalPayoutCurrency(Bond bond, long days) {
        BigDecimal couponsLeft = BigDecimal.valueOf(days)
                .divide(BigDecimal.valueOf(bond.couponPeriod()), 0, RoundingMode.CEILING);
        BigDecimal totalCoupons = bond.couponAmount().multiply(couponsLeft, MC);
        return bond.nominal().add(totalCoupons, MC);
    }

    private BigDecimal calculateNetProfitRub(BigDecimal investedRub, BigDecimal totalPayoutRub) {
        BigDecimal grossProfitRub = totalPayoutRub.subtract(investedRub, MC);
        BigDecimal tax = BigDecimal.ZERO;
        if (grossProfitRub.compareTo(BigDecimal.ZERO) > 0) {
            tax = grossProfitRub.multiply(TAX_RATE, MC);
        }
        return totalPayoutRub.subtract(tax, MC).subtract(investedRub, MC);
    }

    private BigDecimal calculateSimpleYield(BigDecimal roi, BigDecimal multiplier) {
        return roi.multiply(multiplier, MC).multiply(HUNDRED, MC);
    }

    private BigDecimal calculateCurrencyEffect(BigDecimal entry, BigDecimal target, BigDecimal multiplier) {
        return target.divide(entry, MC)
                .subtract(BigDecimal.ONE, MC)
                .multiply(multiplier, MC)
                .multiply(HUNDRED, MC);
    }

    //тут будет вся логика расчетов, распределенная по функциям
}
