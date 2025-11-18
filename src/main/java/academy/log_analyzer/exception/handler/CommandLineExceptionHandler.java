package academy.log_analyzer.exception.handler;

import java.io.PrintWriter;
import picocli.CommandLine;

public class CommandLineExceptionHandler implements CommandLine.IParameterExceptionHandler {
    public int handleParseException(CommandLine.ParameterException ex, String[] args) {
        PrintWriter err = ex.getCommandLine().getErr();
        err.println(ex.getMessage());
        return ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
    }
}
