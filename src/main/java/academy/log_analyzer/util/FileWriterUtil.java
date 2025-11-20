package academy.log_analyzer.util;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileWriterUtil {

    public static void writeFile(String text, String outputPath) {
        try {
            Files.writeString(
                Path.of(outputPath),
                text,
                StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при записи файла " + outputPath, e);
        }
    }

}
