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


    public CalculatorService(MoexDataService moexDataService) {
        this.moexDataService = moexDataService;
    }

    public BondResponse calculateYield(BondRequest userRequest) {
        log.info("Запрошен расчет для ISIN: {}", userRequest.isin());

        BondRequest moexData = moexDataService.getBondFullData(userRequest.isin());


        BigDecimal nominal = (userRequest.nominal() != null) ? userRequest.nominal() : moexData.nominal();
        BigDecimal purchasePrice = (userRequest.purchasePrice() != null) ? userRequest.purchasePrice() : moexData.purchasePrice();
        BigDecimal couponAmount = (userRequest.couponAmount() != null) ? userRequest.couponAmount() : moexData.couponAmount();
        Integer couponPeriodInt = (userRequest.couponPeriod() != null) ? userRequest.couponPeriod() : moexData.couponPeriod();
        LocalDate maturityDate = LocalDate.parse(userRequest.maturityDate() != null ? userRequest.maturityDate() : moexData.maturityDate());
        BigDecimal nkd = (userRequest.nkd() != null) ? userRequest.nkd() : moexData.nkd();
        BigDecimal entryRate = userRequest.entryRate();
        BigDecimal targetRate = userRequest.targetRate();
        LocalDate purchaseDate = LocalDate.now();

        long daysToMaturityLong = ChronoUnit.DAYS.between(purchaseDate, maturityDate);

        if (daysToMaturityLong <= 0) {
            throw new IllegalArgumentException("Дата погашения должна быть позже даты покупки");
        }
        BigDecimal daysToMaturity = BigDecimal.valueOf(daysToMaturityLong);


        //корявые расчеты ии, которые надо будет переписать

        // 2. Грязная цена в валюте: Nominal * (purchasePrice / 100) + NKD
        BigDecimal priceRatio = purchasePrice.divide(HUNDRED, MC);
        BigDecimal cleanPriceCurr = nominal.multiply(priceRatio, MC);
        BigDecimal dirtyPriceCurr = cleanPriceCurr.add(nkd, MC);

        // 3. Инвестировано рублей: DirtyPriceCurr * entryRate
        BigDecimal investedRub = dirtyPriceCurr.multiply(entryRate, MC);

        // 4. Считаем оставшиеся купоны.
        // Используем точное дробное количество периодов для простой доходности
        BigDecimal couponPeriod = BigDecimal.valueOf(couponPeriodInt);
        BigDecimal totalPeriodsLeft = daysToMaturity.divide(couponPeriod, MC);

        // Сумма всех будущих купонов в валюте
        BigDecimal sumCouponsCurr = couponAmount.multiply(totalPeriodsLeft, MC);

        // 5. Итоговая выплата в рублях по целевому курсу: (Номинал + Все купоны) * targetRate
        BigDecimal totalReturnCurr = nominal.add(sumCouponsCurr, MC);
        BigDecimal totalReturnRub = totalReturnCurr.multiply(targetRate, MC);

        // 6. Считаем YTM по формуле: ((TotalReturnRub / InvestedRub) - 1) * (365 / DaysToMaturity) * 100
        BigDecimal roi = totalReturnRub.divide(investedRub, MC).subtract(BigDecimal.ONE, MC); // Return On Investment (абсолютный прирост)
        BigDecimal annualizedMultiplier = DAYS_IN_YEAR.divide(daysToMaturity, MC); // Годовой множитель

        BigDecimal ytmRub = roi.multiply(annualizedMultiplier, MC).multiply(HUNDRED, MC);

        // Округляем финальный результат до 2 знаков после запятой
        // Годовой купон в валюте (сколько долларов/евро получим за год)
        BigDecimal couponsPerYear = DAYS_IN_YEAR.divide(couponPeriod, MC);
        BigDecimal annualCouponCurr = couponAmount.multiply(couponsPerYear, MC);

        // А) Купонная доходность (от номинала)
        BigDecimal couponYield = annualCouponCurr.divide(nominal, MC).multiply(HUNDRED, MC);

        // Б) Текущая доходность (от цены покупки)
        BigDecimal currentYield = annualCouponCurr.divide(dirtyPriceCurr, MC).multiply(HUNDRED, MC);

        // В) Доходность при девальвации за 1 год (прогнозная доходность, если держать бумагу 1 год)
        // Формула: ((Цена_в_валюте + Купоны_за_год) * Курс_прогноз / Инвестировано_руб - 1) * 100
        BigDecimal valueAfterOneYearRub = dirtyPriceCurr.add(annualCouponCurr, MC).multiply(targetRate, MC);
        BigDecimal simpleYearlyYieldRub = valueAfterOneYearRub.divide(investedRub, MC).subtract(BigDecimal.ONE, MC).multiply(HUNDRED, MC);


        return new BondResponse(
                userRequest.isin(),
                couponYield,
                currentYield,
                simpleYearlyYieldRub,
                ytmRub

        );
    }
}