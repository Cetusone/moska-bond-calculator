package org.example.moskacalculatorservice;

import org.example.moskacalculatorservice.api.MoexDataDto;
import org.example.moskacalculatorservice.api.MoexDataService;
import org.example.moskacalculatorservice.api.TDataDto;
import org.example.moskacalculatorservice.api.TDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);
    private final MoexDataService moexDataService;
    private final TDataService tDataService;
    private final BondCalculatorEngine bondCalculatorEngine;
    private final BondAssembler bondAssembler;


    public CalculatorService(MoexDataService moexDataService, BondCalculatorEngine bondCalculatorEngine,  BondAssembler bondAssembler, TDataService tDataService) {
        this.bondCalculatorEngine = bondCalculatorEngine;
        this.moexDataService = moexDataService;
        this.bondAssembler = bondAssembler;
        this.tDataService = tDataService;
    }

    public BondResponse calculateYield(BondRequestDto userRequest) {
        log.info("Запрошен расчет для ISIN: {}", userRequest.isin());


        MoexDataDto moexData = moexDataService.getBondFullData(userRequest.isin());
        TDataDto tData = tDataService.getBondFullData(userRequest.isin());
        Bond tBond = bondAssembler.createBond(userRequest, tData);
        //Bond moexBond = bondAssembler.createBond(userRequest, moexData);
        //YieldCalculation yieldCalculation = bondCalculatorEngine.simpleCalculate(bond);
        YieldCalculation yieldCalculation = bondCalculatorEngine.simpleCalculate(tBond);
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