package org.example.moskacalculatorservice;

import org.example.moskacalculatorservice.api.MoexDataDto;
import org.example.moskacalculatorservice.api.TDataDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class BondAssembler {
    // этот класс будет получать разные dto с api или бд или от пользователя и создавать обычный bond


        // это надо будет в дальнейшем расширить. переписать BondRequestDto для moex отдельно
        public Bond createBond(BondRequestDto userRequest, MoexDataDto moexDataRequest) {

            return new Bond(
                    userRequest.isin(),
                    getOrDefault(userRequest.currency(), moexDataRequest.currency()),
                    getOrDefault(userRequest.nominal(), moexDataRequest.nominal()),
                    getOrDefault(userRequest.couponAmount(), moexDataRequest.couponAmount()),
                    getOrDefault(userRequest.couponPeriod(), moexDataRequest.couponPeriod()),
                    LocalDate.parse(getOrDefault(userRequest.maturityDate(), moexDataRequest.maturityDate())),
                    getOrDefault(userRequest.purchasePrice(), moexDataRequest.purchasePrice()),
                    getOrDefault(userRequest.nkd(), moexDataRequest.nkd()),

                    userRequest.entryRate(),
                    userRequest.targetRate(),
                    LocalDate.now()
            );

        }

        public Bond createBond(BondRequestDto userRequest, TDataDto tDataRequest) {
            return new Bond(
                    userRequest.isin(),
                    getOrDefault(userRequest.currency(), tDataRequest.currency()),
                    getOrDefault(userRequest.nominal(), tDataRequest.nominal()),
                    getOrDefault(userRequest.couponAmount(), tDataRequest.couponAmount()),
                    getOrDefault(userRequest.couponPeriod(), tDataRequest.couponPeriod()),
                    LocalDate.parse(getOrDefault(userRequest.maturityDate(), tDataRequest.maturityDate())),
                    getOrDefault(userRequest.purchasePrice(), tDataRequest.purchasePrice()),
                    getOrDefault(userRequest.nkd(), tDataRequest.nkd()),

                    userRequest.entryRate(),
                    userRequest.targetRate(),
                    LocalDate.now()

            );
        }


    private <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

}
