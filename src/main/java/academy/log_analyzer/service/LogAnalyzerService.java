package academy.log_analyzer.service;

import academy.log_analyzer.entity.AnalyzedFile;
import academy.log_analyzer.entity.LogEntry;
import academy.log_analyzer.entity.StatisticsReport;
import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.exception.InvalidFormatFlagException;
import academy.log_analyzer.format.AdocFormatter;
import academy.log_analyzer.format.FormatType;
import academy.log_analyzer.format.Formatter;
import academy.log_analyzer.format.JsonFormatter;
import academy.log_analyzer.format.MarkdownFormatter;
import academy.log_analyzer.util.FileWriterUtil;
import academy.log_analyzer.util.ParseUtil;
import academy.log_analyzer.util.PathUtil;
import academy.log_analyzer.util.TimeRangeMode;
import academy.log_analyzer.util.TimeRangeUtil;
import academy.log_analyzer.validation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class LogAnalyzerService {

    private final static InputValidator inputValidator = new InputValidator();
    private final static PathUtil pathUtil = new PathUtil();
    private static final Logger log = LoggerFactory.getLogger(LogAnalyzerService.class);

    public void runAnalysis(List<String> paths, String format, String output, LocalDateTime from, LocalDateTime to)
        throws InvalidFileFormatException, IOException, DirectoryNotWritableException, InvalidFormatFlagException {

        FormatType formatType = FormatType.fromValue(format);

        inputValidator.validateOutputFlag(output, formatType);
        inputValidator.validateFromAndTo(from, to);

        TimeRangeUtil timeRangeUtil = new TimeRangeUtil(from, to);

        List<AnalyzedFile> readerList = pathUtil.getAllBufferedReadersFromPaths(paths);

        StatisticsReport statisticsReport = analyzeLogs(readerList, formatType, timeRangeUtil, output);

        Formatter formatter = getFormatter(formatType);

        String reportInString = formatter.format(statisticsReport);

        System.out.println(reportInString);

        FileWriterUtil.writeFile(reportInString, output);
    }

    private StatisticsReport analyzeLogs(List<AnalyzedFile> analyzedFiles, FormatType formatType, TimeRangeUtil timeRangeUtil, String output) {
        LogStatisticsCollector statisticsCollector = new LogStatisticsCollector();
        ParseUtil parseUtil = new ParseUtil();

        for (AnalyzedFile analyzedFile: analyzedFiles) {
            try (BufferedReader reader = analyzedFile.reader()) {
                String logString;

                while ((logString = reader.readLine()) != null) {
                    LogEntry logEntry = parseUtil.parseNginxLog(logString);

                    if (logEntry == null) {
                        log.warn("Не удалось считать строку");
                    } else {
                        if (timeRangeUtil.isCorrectTimeRangeForLogEntry(logEntry)) {
                            statisticsCollector.addLogInStatistics(logEntry);
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return statisticsCollector.getReport(analyzedFiles);
    }

    private Formatter getFormatter(FormatType formatType) {
        return switch (formatType) {
            case JSON -> new JsonFormatter();
            case MARKDOWN -> new MarkdownFormatter();
            case ADOC -> new AdocFormatter();
            default -> new JsonFormatter();
        };
    }

}
