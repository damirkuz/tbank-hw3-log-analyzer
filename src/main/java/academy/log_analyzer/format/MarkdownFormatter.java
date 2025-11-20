package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;
import java.util.Map;

public class MarkdownFormatter extends AbstractFormatter {

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
        sb.append("#### Общая информация\n\n");

        sb.append("|        Метрика        |     Значение |\n");
        sb.append("|:---------------------:|-------------:|\n");

        String files = String.join(", ", r.analyzedFilesNames());
        sb.append("|       Файл(-ы)        | `")
            .append(files)
            .append("` |\n");

        String fromDate = r.from() != null ? r.from().toLocalDate().format(formatterToDDMMYYYY) : "-";
        sb.append("|    Начальная дата     |   ")
            .append(fromDate)
            .append(" |\n");

        String toDate = r.to() != null ? r.to().toLocalDate().format(formatterToDDMMYYYY) : "-";
        sb.append("|     Конечная дата     |   ")
            .append(toDate)
            .append(" |\n");

        sb.append("|  Количество запросов  |       ")
            .append(formatNumber(r.allRequestsCount()))
            .append(" |\n");

        sb.append("| Средний размер ответа |         ")
            .append(formatNumber(Math.round(r.averageResponseSize())))
            .append("b |\n");

        sb.append("|  95p размера ответа   |         ")
            .append(formatNumber(Math.round(r.percentile95())))
            .append("b |\n");
    }

    private void appendResources(StringBuilder sb, StatisticsReport r) {
        sb.append("#### Запрашиваемые ресурсы\n\n");

        sb.append("|     Ресурс      | Количество |\n");
        sb.append("|:---------------:|-----------:|\n");

        for (Map.Entry<String, Long> entry : r.requestedPaths().entrySet()) {
            sb.append("| ")
                .append(entry.getKey())
                .append(" |      ")
                .append(formatNumber(entry.getValue()))
                .append(" |\n");
        }
    }

    private void appendResponseCodes(StringBuilder sb, StatisticsReport r) {
        sb.append("#### Коды ответа\n\n");

        sb.append("| Код |          Имя          | Количество |\n");
        sb.append("|:---:|:---------------------:|-----------:|\n");

        for (Map.Entry<Integer, Long> entry : r.statusCodesStatistics().entrySet()) {
            int code = entry.getKey();
            long count = entry.getValue();
            String name = getStatusName(code);

            sb.append("| ")
                .append(code)
                .append(" | ")
                .append(padCenter(name, 21))
                .append(" |       ")
                .append(formatNumber(count))
                .append(" |\n");
        }
    }

    private String formatNumber(long value) {
        String s = Long.toString(value);
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        int firstGroup = len % 3 == 0 ? 3 : len % 3;

        sb.append(s, 0, firstGroup);
        for (int i = firstGroup; i < len; i += 3) {
            sb.append('_').append(s, i, i + 3);
        }
        return sb.toString();
    }

    private String padCenter(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        int totalPadding = width - text.length();
        int left = totalPadding / 2;
        int right = totalPadding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private String getStatusName(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "-";
        };
    }
}
