package academy.log_analyzer.exception.handler;


import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import picocli.CommandLine;
import java.io.IOException;

public class ExitCodeMapper implements CommandLine.IExitCodeExceptionMapper {

    @Override
    public int getExitCode(Throwable t) {
//        - `0` - программа успешно завершила свою работу
//        - `1` - непредвиденная ошибка
//        - `2` - некорректное использование программы (неверные параметры, отсутствие файлов и т.д.)

        if (t instanceof IOException) return 2;
        if (t instanceof IllegalArgumentException) return 2;
        //  непредвиденная ошибка
        return 1;
    }
}
