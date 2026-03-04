package org.example.moskacalculatorservice.api;


import org.example.moskacalculatorservice.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Coupon;
import ru.tinkoff.piapi.contract.v1.InstrumentShort;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TDataService implements DataService {

    // Spring Boot Starter любезно предоставит этот бин автоматически
    private final InvestApi investApi;
    private final Logger log = LoggerFactory.getLogger(TDataService.class);

    public TDataService(InvestApi investApi) {
        this.investApi = investApi;
    }


    public void fetchTradableBonds() {
        log.info("Начинаем загрузку списка облигаций...");

        // Синхронный вызов для получения всех торгуемых облигаций
        List<Bond> bonds = investApi.getInstrumentsService().getTradableBondsSync();

        for (Bond bond : bonds) {
            log.info("Тикер: {}, Название: {}, FIGI: {}",
                    bond.getTicker(), bond.getName(), bond.getFigi());

            // Здесь будет логика извлечения номинала, купона и валюты
        }
    }

    @Override
    public TDataDto getBondFullData(String isin) {
        log.info("Начинаем загрузку данных для ISIN: {}", isin);

        List<InstrumentShort> foundInstruments = investApi.getInstrumentsService().findInstrumentSync(isin);

        if (foundInstruments.isEmpty()) {
            log.warn("Облигация с ISIN {} не найдена в Тинькофф", isin);
            throw new IllegalArgumentException("Инструмент с ISIN " + isin + " не найден");
        }

        String figi = foundInstruments.get(0).getFigi();
        log.info("Для ISIN {} успешно найден FIGI: {}", isin, figi);

        Bond bond = investApi.getInstrumentsService().getBondByFigiSync(figi);

        List<LastPrice> lastPrices = investApi.getMarketDataService().getLastPricesSync(List.of(figi));
        BigDecimal priceInPercent = lastPrices.isEmpty() ? BigDecimal.ZERO
                : TInvestMapper.toBigDecimal(lastPrices.get(0).getPrice());

        BigDecimal nominal = TInvestMapper.toBigDecimal(bond.getNominal());
        BigDecimal nkd = TInvestMapper.toBigDecimal(bond.getAciValue());

        BigDecimal purchasePrice = nominal.multiply(priceInPercent)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        Instant now = Instant.now();
        List<Coupon> coupons = investApi.getInstrumentsService()
                .getBondCouponsSync(figi, now, now.plus(365, ChronoUnit.DAYS));

        BigDecimal couponAmount = BigDecimal.ZERO;
        Integer couponPeriod = 0;

        if (!coupons.isEmpty()) {
            Coupon nextCoupon = coupons.get(0);
            couponAmount = TInvestMapper.toBigDecimal(nextCoupon.getPayOneBond());
            couponPeriod = nextCoupon.getCouponPeriod(); //api само дает период в днях
        }

        String maturityDate = Instant.ofEpochSecond(bond.getMaturityDate().getSeconds())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE); // "yyyy-MM-dd"

        Currency currency = mapCurrency(bond.getCurrency());

        return new TDataDto(
                isin,
                currency,
                nominal,
                couponAmount,
                couponPeriod,
                maturityDate,
                purchasePrice,
                nkd,
                null,
                null
        );
    }

    private Currency mapCurrency(String apiCurrencyStr) {
        return switch (apiCurrencyStr.toLowerCase()) {
            case "rub" -> Currency.RUB;
            case "usd" -> Currency.USD;
            case "eur" -> Currency.EUR;
            case "cny" -> Currency.CNY;
            default -> throw new IllegalArgumentException("Неизвестная валюта: " + apiCurrencyStr);
        };
    }
}