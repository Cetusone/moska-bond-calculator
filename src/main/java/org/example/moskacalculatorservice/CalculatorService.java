package org.example.moskacalculatorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);
    private final MoexDataService moexDataService;
    private final BondCalculatorEngine bondCalculatorEngine;
    private final BondAssembler bondAssembler;


    public CalculatorService(MoexDataService moexDataService, BondCalculatorEngine bondCalculatorEngine,  BondAssembler bondAssembler) {
        this.bondCalculatorEngine = bondCalculatorEngine;
        this.moexDataService = moexDataService;
        this.bondAssembler = bondAssembler;
    }

    public BondResponse calculateYield(BondRequestDto userRequest) {
        log.info("Запрошен расчет для ISIN: {}", userRequest.isin());


        BondRequestDto moexData = moexDataService.getBondFullData(userRequest.isin());
        Bond bond = bondAssembler.createBond(userRequest, moexData);
        YieldCalculation yieldCalculation = bondCalculatorEngine.simpleCalculate(bond);

        return new BondResponse(
                yieldCalculation.isin(),
                yieldCalculation.yieldInCurrency(),
                yieldCalculation.grossYieldInRub(),
                yieldCalculation.netYieldInRub(),
                yieldCalculation.currencyEffect()

        );
    }
}
//здесь у меня происходит нарушение solid т.к. этот класс и собирает BondRequest и выполняет расчет доходности.