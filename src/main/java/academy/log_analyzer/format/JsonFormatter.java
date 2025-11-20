package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonFormatter extends AbstractFormatter {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);


    @Override
    public String format(StatisticsReport statisticsReport) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("files", statisticsReport.analyzedFilesNames());
        map.put("totalRequestsCount", statisticsReport.allRequestsCount());

        Map<String, Object> responseSizeInBytes = new LinkedHashMap<>();
        responseSizeInBytes.put("average", statisticsReport.averageResponseSize());
        responseSizeInBytes.put("max", statisticsReport.maxResponseSizeRequest());
        responseSizeInBytes.put("p95", statisticsReport.percentile95());
        map.put("responseSizeInBytes", responseSizeInBytes);

        map.put("resources",
            transformMapToJson(statisticsReport.requestedPaths(), "resource", "totalRequestsCount"));
        map.put("responseCodes",
            transformMapToJson(statisticsReport.statusCodesStatistics(), "code", "totalResponsesCount"));
        map.put("requestsPerDate",
            transformDatesToJson(statisticsReport.requestsInDate(), statisticsReport.allRequestsCount()));
        map.put("uniqueProtocols", statisticsReport.uniqueProtocols());

        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <K, V> List<Map<String, Object>> transformMapToJson(Map<K, V> map,
                                                                String keyName,
                                                                String valueName) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (K key : map.keySet()) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            linkedHashMap.put(keyName, key);
            linkedHashMap.put(valueName, map.get(key));
            result.add(linkedHashMap);
        }
        return result;
    }

    private List<Map<String, Object>> transformDatesToJson(Map<LocalDate, Long> dates,
                                                           long allRequestsCount) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalDate date : dates.keySet()) {
            Map<String, Object> linkedHashMap = new LinkedHashMap<>();
            linkedHashMap.put("date", date.format(formatterToYYYYMMDD));
            linkedHashMap.put("weekday", date.getDayOfWeek().toString());
            long count = dates.get(date);
            linkedHashMap.put("totalRequestsCount", count);
            linkedHashMap.put("totalRequestsPercentage",
                getTotalRequestsPercentage(allRequestsCount, count));

            result.add(linkedHashMap);
        }
        return result;
    }
}
