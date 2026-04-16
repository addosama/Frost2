package pub.frost.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    public static <T extends Number & Comparable<T>> T clamp(T value, T min, T max) {
        return max(min, min(max, value));
    }

    public static <T extends Number & Comparable<T>> T max(T value1, T value2) {
        return value1.compareTo(value2) > 0? value1 : value2;
    }

    public static <T extends Number & Comparable<T>> T min(T value1, T value2) {
        return value1.compareTo(value2) < 0? value1 : value2;
    }

    public static BigDecimal roundToStep(BigDecimal value, BigDecimal increasingStep) {
        if (increasingStep.equals(BigDecimal.ZERO)) return value;
        try {
            return BigDecimal.valueOf(value.divide(increasingStep, increasingStep.scale(), RoundingMode.DOWN).intValue()).multiply(increasingStep);
        } catch (ArithmeticException e) {
            return value;
        }
    }

    public static double lerp(double a, double b, float delta) {
        return a + (b - a) * delta;
    }
}
