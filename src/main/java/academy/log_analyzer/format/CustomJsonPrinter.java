package academy.log_analyzer.format;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import java.io.IOException;

public class CustomJsonPrinter extends DefaultPrettyPrinter {

    public CustomJsonPrinter() {
        super();
        // Настраиваем отступы для массивов
        this.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    public CustomJsonPrinter(CustomJsonPrinter base) {
        super(base);
    }

    @Override
    public DefaultPrettyPrinter createInstance() {
        return new CustomJsonPrinter(this);
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        // Убираем пробел перед двоеточием
        g.writeRaw(": ");
    }
}
