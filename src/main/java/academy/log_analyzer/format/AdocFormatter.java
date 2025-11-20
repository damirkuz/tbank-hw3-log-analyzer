package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;
import java.util.Map;

public class AdocFormatter extends AbstractFormatter {

    @Override
    public String format(StatisticsReport statisticsReport) {
        StringBuilder sb = new StringBuilder();

        appendGeneralInfo(sb, statisticsReport);
        sb.append("\n");
        appendResources(sb, statisticsReport);
        sb.append("\n");
        appendResponseCodes(sb, statisticsReport);

        return sb.toString();
    }

    private void appendGeneralInfo(StringBuilder sb, StatisticsReport r) {
        sb.append("=== Общая информация\n\n");

        sb.append(".Общая информация\n");
        sb.append("[options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("| Метрика | Значение\n");

        String files = String.join(", ", r.analyzedFilesNames());
        String fromDate = r.from() != null ? r.from().toLocalDate().format(formatterToDDMMYYYY) : "-";
        String toDate = r.to() != null ? r.to().toLocalDate().format(formatterToDDMMYYYY) : "-";

        sb.append("| Файл(-ы) | `").append(files).append("`\n");
        sb.append("| Начальная дата | ").append(fromDate).append("\n");
        sb.append("| Конечная дата | ").append(toDate).append("\n");
        sb.append("| Количество запросов | ")
                .append(formatNumber(r.allRequestsCount()))
                .append("\n");
        sb.append("| Средний размер ответа | ")
                .append(formatNumber(Math.round(r.averageResponseSize())))
                .append("b\n");
        sb.append("| 95p размера ответа | ")
                .append(formatNumber(Math.round(r.percentile95())))
                .append("b\n");

        sb.append("|===\n");
    }

    private void appendResources(StringBuilder sb, StatisticsReport r) {
        sb.append("=== Запрашиваемые ресурсы\n\n");

        sb.append(".Запрашиваемые ресурсы\n");
        sb.append("[options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("| Ресурс | Количество\n");

        for (Map.Entry<String, Long> entry : r.requestedPaths().entrySet()) {
            sb.append("| ")
                    .append(entry.getKey())
                    .append(" | ")
                    .append(formatNumber(entry.getValue()))
                    .append("\n");
        }

        sb.append("|===\n");
    }

    private void appendResponseCodes(StringBuilder sb, StatisticsReport r) {
        sb.append("=== Коды ответа\n\n");

        sb.append(".Коды ответа\n");
        sb.append("[options=\"header\"]\n");
        sb.append("|===\n");
        sb.append("| Код | Имя | Количество\n");

        for (Map.Entry<Integer, Long> entry : r.statusCodesStatistics().entrySet()) {
            int code = entry.getKey();
            long count = entry.getValue();
            String name = getStatusName(code);

            sb.append("| ")
                    .append(code)
                    .append(" | ")
                    .append(name)
                    .append(" | ")
                    .append(formatNumber(count))
                    .append("\n");
        }

        sb.append("|===\n");
    }
}
