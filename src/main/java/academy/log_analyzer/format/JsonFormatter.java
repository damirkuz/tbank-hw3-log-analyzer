package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonFormatter extends AbstractFormatter {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private static final DefaultPrettyPrinter prettyPrinter = new CustomJsonPrinter();

    @Override
    public String format(StatisticsReport statisticsReport) {
        Map<String, Object> json = buildJson(statisticsReport);
        try {
            return objectMapper.writer(prettyPrinter).writeValueAsString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> buildJson(StatisticsReport report) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("files", report.analyzedFilesNames());
        map.put("totalRequestsCount", report.allRequestsCount());
        map.put("responseSizeInBytes", buildResponseSizeBlock(report));
        map.put("resources", transformMapToJson(report.requestedPaths(), "resource", "totalRequestsCount"));
        map.put("responseCodes", transformMapToJson(report.statusCodesStatistics(), "code", "totalResponsesCount"));
        map.put("requestsPerDate", transformDatesToJson(report.requestsInDate(), report.allRequestsCount()));
        map.put("uniqueProtocols", report.uniqueProtocols());

        return map;
    }

    private Map<String, Object> buildResponseSizeBlock(StatisticsReport report) {
        Map<String, Object> responseSizeInBytes = new LinkedHashMap<>();
        responseSizeInBytes.put("average", round(report.averageResponseSize(), 2));
        responseSizeInBytes.put("max", round(report.maxResponseSizeRequest(), 1));
        responseSizeInBytes.put("p95", round(report.percentile95(), 1));
        return responseSizeInBytes;
    }

    private <K, V> List<Map<String, Object>> transformMapToJson(
        Map<K, V> map,
        String keyName,
        String valueName
    ) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (K key : map.keySet()) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            linkedHashMap.put(keyName, key);
            linkedHashMap.put(valueName, map.get(key));
            result.add(linkedHashMap);
        }
        return result;
    }

    private List<Map<String, Object>> transformDatesToJson(Map<LocalDate, Long> dates, long allRequestsCount) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<LocalDate> sortedDates = new ArrayList<>(dates.keySet());
        Collections.sort(sortedDates);

        for (LocalDate date : sortedDates) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            linkedHashMap.put("date", date.format(formatterToYYYYMMDD));
            linkedHashMap.put("weekday", capitalize(date.getDayOfWeek().toString()));
            long count = dates.get(date);
            linkedHashMap.put("totalRequestsCount", count);
            linkedHashMap.put("totalRequestsPercentage",
                round(getTotalRequestsPercentage(allRequestsCount, count), 2));

            result.add(linkedHashMap);
        }
        return result;
    }

    private static double round(double value, int scale) {
        return BigDecimal.valueOf(value)
            .setScale(scale, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String lower = s.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
