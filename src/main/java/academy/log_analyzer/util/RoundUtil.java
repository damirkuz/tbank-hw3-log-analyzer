package academy.log_analyzer.util;

public class RoundUtil {
    public static double roundTo2digitsAfterDot(double number) {
        return Math.round(number * 100.0) / 100.0;
    }
}
