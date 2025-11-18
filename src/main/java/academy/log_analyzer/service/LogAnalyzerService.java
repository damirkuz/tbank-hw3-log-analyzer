package academy.log_analyzer.service;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.format.FormatType;
import academy.log_analyzer.util.PathUtil;
import academy.log_analyzer.validation.InputValidator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class LogAnalyzerService {
    public void analyze(String path, String format, String output, Date from, Date to)
        throws InvalidFileFormatException, IOException, DirectoryNotWritableException {

        InputValidator inputValidator = new InputValidator();

        PathUtil pathUtil = new PathUtil();
        List<Path> paths = pathUtil.getAllPaths(path);

        FormatType formatType = FormatType.valueOf(format);

        inputValidator.validateOutputFlag(output, formatType);


    }
}
