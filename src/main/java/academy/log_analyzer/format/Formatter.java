package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;

public interface Formatter {
    String format(StatisticsReport statisticsReport);
}
