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
import academy.log_analyzer.validation.InputValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAnalyzerService {

    private static final InputValidator inputValidator = new InputValidator();
    private static final PathUtil pathUtil = new PathUtil();
    private static final ParseUtil parseUtil = new ParseUtil();
    private static final Logger log = LoggerFactory.getLogger(LogAnalyzerService.class);

    public void runAnalysis(List<String> paths, String format, String output, String stringFrom, String stringTo)
            throws InvalidFileFormatException, IOException, DirectoryNotWritableException, InvalidFormatFlagException {

        log.info("Начало анализа логов");

        FormatType formatType = FormatType.fromValue(format);
        inputValidator.validateOutputFlag(output, formatType);

        LocalDateTime from = parseUtil.parseLocalDateTime(stringFrom);
        LocalDateTime to = parseUtil.parseLocalDateTime(stringTo);
        inputValidator.validateFromAndTo(from, to);

        TimeRangeService timeRangeService = new TimeRangeService(from, to);

        log.info("Чтение файлов");
        List<AnalyzedFile> readerList = pathUtil.getAllBufferedReadersFromPaths(paths);

        log.info("Анализ логов");
        StatisticsReport statisticsReport = analyzeLogs(readerList, timeRangeService);

        log.info("Формирование отчета");
        Formatter formatter = getFormatter(formatType);
        String reportInString = formatter.format(statisticsReport);

        log.info("Сохранение результата");
        FileWriterUtil.writeFile(reportInString, output);

        log.info("Анализ завершен");
    }

    private StatisticsReport analyzeLogs(List<AnalyzedFile> analyzedFiles, TimeRangeService timeRangeService) {
        LogStatisticsCollector statisticsCollector = new LogStatisticsCollector();

        for (AnalyzedFile analyzedFile : analyzedFiles) {
            try (BufferedReader reader = analyzedFile.reader()) {
                String logString;

                while ((logString = reader.readLine()) != null) {
                    LogEntry logEntry = parseUtil.parseNginxLog(logString);

                    if (logEntry == null) {
                        log.warn("Не удалось считать строку");
                    } else {
                        if (timeRangeService.isCorrectTimeRangeForLogEntry(logEntry)) {
                            statisticsCollector.addLogInStatistics(logEntry);
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Ошибка чтения файла");
            }
        }

        return statisticsCollector.getReport(
                analyzedFiles, timeRangeService.getLocalDateTimeFrom(), timeRangeService.getLocalDateTimeTo());
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
