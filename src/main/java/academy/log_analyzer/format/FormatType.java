package academy.log_analyzer.format;

import academy.log_analyzer.exception.InvalidFormatFlagException;
import com.fasterxml.jackson.databind.util.JSONPObject;

public enum FormatType {
    JSON ("json"),
    MARKDOWN ("markdown"),
    ADOC ("adoc");

    private final String value;

    FormatType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public FormatType fromValue(String s) throws InvalidFormatFlagException {
        for (FormatType formatType: FormatType.values()) {
            if (formatType.getValue().equalsIgnoreCase(s)) {
                return formatType;
            }
        }

        throw new InvalidFormatFlagException("Некорректный формат файла для вывода");
    }
}
