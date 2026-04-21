package pub.frost.utils.animations;

import lombok.RequiredArgsConstructor;

import java.util.function.DoubleUnaryOperator;

import static pub.frost.utils.animations.EnumEasingFunction.MagicNumbers.*;

@RequiredArgsConstructor
public enum EnumEasingFunction implements DoubleUnaryOperator {
    LINEAR(
            "Linear",
            x -> x
    ),

    EASE_IN_SINE(
            "EaseInSine",
            x -> 1 - Math.cos((x * Math.PI) / 2)
    ),
    EASE_OUT_SINE(
            "EaseOutSine",
            x -> Math.sin((x * Math.PI) / 2)
    ),
    EASE_IN_OUT_SINE(
            "EaseInOutSine",
            x -> -(Math.cos(Math.PI * x) - 1) / 2
    ),

    EASE_IN_QUAD(
            "EaseInQuad",
            x -> x * x
    ),
    EASE_OUT_QUAD(
            "EaseOutQuad",
            x -> 1 - (1 - x) * (1 - x)
    ),
    EASE_IN_OUT_QUAD(
            "EaseInOutQuad",
            x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2
    ),

    EASE_IN_CUBIC(
            "EaseInCubic",
            x -> x * x * x
    ),
    EASE_OUT_CUBIC(
            "EaseOutCubic",
            x -> 1 - Math.pow(1 - x, 3)
    ),
    EASE_IN_OUT_CUBIC(
            "EaseInOutCubic",
            x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2
    ),

    EASE_IN_QUART(
            "EaseInQuart",
            x -> x * x * x * x
    ),
    EASE_OUT_QUART(
            "EaseOutQuart",
            x -> 1 - Math.pow(1 - x, 4)
    ),
    EASE_IN_OUT_QUART(
            "EaseInOutQuart",
            x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2
    ),

    EASE_IN_QUINT(
            "EaseInQuint",
            x -> x * x * x * x * x
    ),
    EASE_OUT_QUINT(
            "EaseOutQuint",
            x -> 1 - Math.pow(1 - x, 5)
    ),
    EASE_IN_OUT_QUINT(
            "EaseInOutQuint",
            x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2
    ),

    EASE_IN_EXPO(
            "EaseInExpo",
            x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10)
    ),
    EASE_OUT_EXPO(
            "EaseOutExpo",
            x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x)
    ),
    EASE_IN_OUT_EXPO(
            "EaseInOutExpo",
            x -> x == 0 ? 0 : x == 1 ? 1 :
                              x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
                              : (2 - Math.pow(2, -20 * x + 10)) / 2
    ),

    EASE_IN_CIRC(
            "EaseInCirc",
            x -> 1 - Math.sqrt(1 - Math.pow(x, 2))
    ),
    EASE_OUT_CIRC(
            "EaseOutCirc",
            x -> Math.sqrt(1 - Math.pow(x - 1, 2))
    ),
    EASE_IN_OUT_CIRC(
            "EaseInOutCirc",
            x -> x < 0.5
                    ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
                    : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2
    ),

    EASE_IN_BACK(
            "EaseInBack",
            x -> c3 * x * x * x - c1 * x * x
    ),
    EASE_OUT_BACK(
            "EaseOutBack",
            x -> 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2)
    ),
    EASE_IN_OUT_BACK(
            "EaseInOutBack",
            x -> x < 0.5
                    ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
                    : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2
    ),

    EASE_IN_ELASTIC(
            "EaseInElastic",
            x -> x == 0 ? 0 : x == 1 ? 1
                              : -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4)
    ),
    EASE_OUT_ELASTIC(
            "EaseOutElastic",
            x -> x == 0 ? 0 : x == 1 ? 1
                              : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1
    ),
    EASE_IN_OUT_ELASTIC(
            "EaseInOutElastic",
            x -> x == 0 ? 0 : x == 1 ? 1
                              : x < 0.5
                                ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
                                : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1
    ),

    EASE_OUT_BOUNCE(
            "EaseOutBounce",
            x -> {
                if (x < 1 / d1) {
                    return n1 * x * x;
                } else if (x < 2 / d1) {
                    x -= 1.5 / d1;
                    return n1 * x * x + 0.75;
                } else if (x < 2.5 / d1) {
                    x -= 2.25 / d1;
                    return n1 * x * x + 0.9375;
                } else {
                    x -= 2.625 / d1;
                    return n1 * x * x + 0.984375;
                }
            }
    ),
    EASE_IN_BOUNCE(
            "EaseInBounce",
            x -> 1 - EASE_OUT_BOUNCE.applyAsDouble(1 - x)
    ),
    EASE_IN_OUT_BOUNCE(
            "EaseInOutBounce",
            x -> x < 0.5
                    ? (1 - EASE_IN_BOUNCE.applyAsDouble(1 - 2 * x)) / 2
                    : (1 + EASE_OUT_BOUNCE.applyAsDouble(2 * x - 1)) / 2
    ),

    ;
    final String key;
    final DoubleUnaryOperator operator;

    @Override
    public double applyAsDouble(double operand) {
        return operator.applyAsDouble(operand);
    }
    public float applyAsFloat(float operand) {
        return (float) operator.applyAsDouble(operand);
    }

    static class MagicNumbers {
        static final double c1 = 1.70158;
        static final double c2 = c1 * 1.525;
        static final double c3 = c1 + 1;
        static final double c4 = (2 * Math.PI) / 3;
        static final double c5 = (2 * Math.PI) / 4.5;
        static final double n1 = 7.5625;
        static final double d1 = 2.75;
    }
}
