package org.example.moskacalculatorservice.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.tinkoff.piapi.core.InvestApi;

@Configuration
public class TInvestConfig {

    @Value("${tbank.token}")
    private String token;

    @Bean
    public InvestApi investApi() {
        return InvestApi.createSandbox(token);
    }
}