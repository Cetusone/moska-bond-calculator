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
        log.info("Подключение к моекс");
        if (data == null) throw new RuntimeException("Инструмент не найден на бирже: " + isin);
        return new BondRequest(
                isin,
                Currency.getCurrency(getStringVal(columns, data, "FACEUNIT")),
                getBigDecimalVal(columns, data, "FACEVALUE"),
                getBigDecimalVal(columns, data, "COUPONVALUE"),
                getIntegerVal(columns, data, "COUPONPERIOD"),
                getStringVal(columns, data, "MATDATE"),
                getBigDecimalVal(columns, data, "PREVPRICE"),
                getBigDecimalVal(columns, data, "ACCRUEDINT"),
                null,
                null
        );
    }

    private String getStringVal(JsonNode columns, JsonNode data, String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).asText().equalsIgnoreCase(columnName)) {
                JsonNode valueNode = data.get(i);
                return (valueNode == null || valueNode.isNull()) ? "" : valueNode.asText();
            }
        }
        return "";
    }
    private Integer getIntegerVal(JsonNode columns, JsonNode data, String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).asText().equalsIgnoreCase(columnName)) {
                JsonNode valueNode = data.get(i);
                return (valueNode == null || valueNode.isNull()) ? null : valueNode.asInt();
            }
        }
        return 0;
    }
    private BigDecimal getBigDecimalVal(JsonNode columns, JsonNode data, String columnName) {
        String val = getStringVal(columns, data, columnName);
        return (val.isEmpty() || val.equalsIgnoreCase("null")) ? BigDecimal.ZERO : new BigDecimal(val);
    }
}