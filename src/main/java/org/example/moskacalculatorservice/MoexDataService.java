package org.example.moskacalculatorservice;
import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

@Service
public class MoexDataService {

    private static final Logger log = LoggerFactory.getLogger(MoexDataService.class);
    private final RestClient restClient;

    public MoexDataService(@Value("${moex.base-url}") String baseUrl,
                           @Value("${moex.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public BondRequest getBondFullData(String isin) {
        JsonNode root = restClient.get()
                .uri("/iss/engines/stock/markets/bonds/securities/{isin}.json?iss.meta=off", isin)
                .retrieve()
                .body(JsonNode.class);

        return parseMoexResponse(root, isin);
    }

    private BondRequest parseMoexResponse(JsonNode root, String isin) {
        JsonNode securities = root.path("securities");
        if (securities.isMissingNode()) {
            throw new RuntimeException("Ответ от MOEX не содержит секции 'securities'");
        }

        JsonNode columns = securities.path("columns");
        JsonNode data = securities.path("data").get(0);

        if (data == null) throw new RuntimeException("Инструмент не найден на бирже: " + isin);

        return new BondRequest(
                isin,
                Currency.RUB,
                getVal(columns, data, "LOTSIZE"),
                null, null, null,
                getVal(columns, data, "PREVPRICE"),
                getVal(columns, data, "ACCRUEDINT"),
                null, null
        );
    }

    private BigDecimal getVal(JsonNode columns, JsonNode data, String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).asText().equalsIgnoreCase(columnName)) {
                String val = data.get(i).asText();
                return (val.isEmpty() || val.equals("null")) ? BigDecimal.ZERO : new BigDecimal(val);
            }
        }
        return BigDecimal.ZERO;
    }
}