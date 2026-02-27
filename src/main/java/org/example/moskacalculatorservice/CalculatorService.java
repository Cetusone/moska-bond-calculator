package org.example.moskacalculatorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);
    private final MoexDataService moexDataService;

    public CalculatorService(MoexDataService moexDataService) {
        this.moexDataService = moexDataService;
    }

    public BondResponse calculateYield(BondRequest userRequest) {
        log.info("Запрошен расчет для ISIN: {}", userRequest.isin());

        BondRequest moexData = moexDataService.getBondFullData(userRequest.isin(), userRequest.entryRate(), userRequest.targetRate());


        BigDecimal nominal = (userRequest.nominal() != null) ? userRequest.nominal() : moexData.nominal();
        BigDecimal price = (userRequest.purchasePrice() != null) ? userRequest.purchasePrice() : moexData.purchasePrice();

        log.info("Данные для расчета. Номинал: {}, Цена (в %): {}", nominal, price);
        return new BondResponse();
    }
}