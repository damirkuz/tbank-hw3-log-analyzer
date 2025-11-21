package academy.acceptance;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tdunning.math.stats.TDigest;

import java.util.Random;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatsCalculationTest {

    @Test
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() {
        fail("Not implemented yet");
    }

    @Test
    @DisplayName("TDigest должен соответствовать 95-му процентилю для нормального распределения")
    void testP95OnNormalDistribution() {
        // Теоретическое значение 95-го перцентиля для N(0, 1) составляет приблизительно 1.64485.
        // Проверяем, что TDigest дает похожий результат

        TDigest digest = TDigest.createDigest(100);

        int sampleSize = 1_000_000;
        Random random = new Random();

        DoubleStream.generate(random::nextGaussian)
            .limit(sampleSize)
            .forEach(digest::add);

        double actualP95 = digest.quantile(0.95);

        double expectedP95 = 1.644853626951;

        double tolerance = 0.01;

        assertEquals(expectedP95, actualP95, tolerance);
    }

}

