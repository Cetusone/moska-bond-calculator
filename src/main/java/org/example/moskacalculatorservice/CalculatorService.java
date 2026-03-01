package org.example.moskacalculatorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);
    private final MoexDataService moexDataService;
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.13"); // НДФЛ 13%

    public CalculatorService(MoexDataService moexDataService) {
        this.moexDataService = moexDataService;
    }

    public BondResponse calculateYield(BondRequest userRequest) {
        log.info("Запрошен расчет для ISIN: {}", userRequest.isin());

        BondRequest moexData = moexDataService.getBondFullData(userRequest.isin());

        BigDecimal nominal = getOrDefault(userRequest.nominal(), moexData.nominal());
        BigDecimal purchasePrice = getOrDefault(userRequest.purchasePrice(), moexData.purchasePrice());
        BigDecimal couponAmount = getOrDefault(userRequest.couponAmount(), moexData.couponAmount());
        Integer couponPeriodInt = getOrDefault(userRequest.couponPeriod(), moexData.couponPeriod());
        LocalDate maturityDate = LocalDate.parse(getOrDefault(userRequest.maturityDate(), moexData.maturityDate()));
        BigDecimal nkd = getOrDefault(userRequest.nkd(), moexData.nkd());

        BigDecimal entryRate = userRequest.entryRate();
        BigDecimal targetRate = userRequest.targetRate();
        LocalDate purchaseDate = LocalDate.now();

        long daysToMaturityLong = ChronoUnit.DAYS.between(purchaseDate, maturityDate);
        if (daysToMaturityLong <= 0) {
            throw new IllegalArgumentException("Дата погашения должна быть позже даты покупки");
        }
        BigDecimal daysToMaturity = BigDecimal.valueOf(daysToMaturityLong);
        BigDecimal annualizedMultiplier = DAYS_IN_YEAR.divide(daysToMaturity, MC);

        BigDecimal priceRatio = purchasePrice.divide(HUNDRED, MC);
        BigDecimal dirtyPriceCurr = nominal.multiply(priceRatio, MC).add(nkd, MC);

        BigDecimal investedRub = dirtyPriceCurr.multiply(entryRate, MC);

        BigDecimal couponPeriod = BigDecimal.valueOf(couponPeriodInt);
        BigDecimal couponsLeft = daysToMaturity.divide(couponPeriod, 0, RoundingMode.CEILING);

        BigDecimal sumCouponsCurr = couponAmount.multiply(couponsLeft, MC);

        BigDecimal totalPayoutCurr = nominal.add(sumCouponsCurr, MC);

        BigDecimal totalPayoutRub = totalPayoutCurr.multiply(targetRate, MC);

        BigDecimal grossProfitRub = totalPayoutRub.subtract(investedRub, MC);
        BigDecimal taxRub = BigDecimal.ZERO;

        //налог
        if (grossProfitRub.compareTo(BigDecimal.ZERO) > 0) {
            taxRub = grossProfitRub.multiply(TAX_RATE, MC);
        }

        BigDecimal netPayoutRub = totalPayoutRub.subtract(taxRub, MC);
        BigDecimal netProfitRub = netPayoutRub.subtract(investedRub, MC);


        //в валюте
        BigDecimal roiCurr = totalPayoutCurr.divide(dirtyPriceCurr, MC).subtract(BigDecimal.ONE, MC);
        BigDecimal yieldInCurrency = roiCurr.multiply(annualizedMultiplier, MC).multiply(HUNDRED, MC);

        //без налога
        BigDecimal roiRubGross = grossProfitRub.divide(investedRub, MC);
        BigDecimal grossYieldInRub = roiRubGross.multiply(annualizedMultiplier, MC).multiply(HUNDRED, MC);


        BigDecimal roiRubNet = netProfitRub.divide(investedRub, MC);
        BigDecimal netYieldInRub = roiRubNet.multiply(annualizedMultiplier, MC).multiply(HUNDRED, MC);

        BigDecimal currencyEffectPercent = targetRate.divide(entryRate, MC)
                .subtract(BigDecimal.ONE, MC)
                .multiply(annualizedMultiplier, MC)
                .multiply(HUNDRED, MC);

        return new BondResponse(
                userRequest.isin(),
                yieldInCurrency.setScale(2, RoundingMode.HALF_UP),
                grossYieldInRub.setScale(2, RoundingMode.HALF_UP),
                netYieldInRub.setScale(2, RoundingMode.HALF_UP),
                netProfitRub.setScale(2, RoundingMode.HALF_UP),
                currencyEffectPercent.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}