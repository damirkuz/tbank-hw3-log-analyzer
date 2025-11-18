package academy.log_analyzer.service;

import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.util.PathUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

public class LogAnalyzerService {
    public void analyze(String path, String format, String output, Date from, Date to)
            throws InvalidFileFormatException, IOException {
        PathUtil pathUtil = new PathUtil();
        List<Path> paths = pathUtil.getAllPaths(path);
    }
}
