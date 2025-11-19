package academy.log_analyzer.service;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.exception.InvalidFormatFlagException;
import academy.log_analyzer.format.FormatType;
import academy.log_analyzer.util.PathUtil;
import academy.log_analyzer.validation.InputValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class LogAnalyzerService {

    private final static InputValidator inputValidator = new InputValidator();
    private final static PathUtil pathUtil = new PathUtil();

    public void analyze(List<String> paths, String format, String output, Date from, Date to)
        throws InvalidFileFormatException, IOException, DirectoryNotWritableException, InvalidFormatFlagException {

        FormatType formatType = FormatType.fromValue(format);

        inputValidator.validateOutputFlag(output, formatType);

        List<BufferedReader> readerList = pathUtil.getAllBufferedReadersFromPaths(paths);


    }

}
