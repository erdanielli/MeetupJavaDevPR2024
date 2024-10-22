package study.meetup.springuicada.application.util;

import java.math.BigDecimal;

public final class BRL {

    private BRL() {
        throw new UnsupportedOperationException();
    }

    /// | double input | long output |
    /// | ------------ | ----------- |
    /// |  0.01        |    1        |
    /// |   0.1        |   10        |
    /// |   1.0        |  100        |
    /// | 10.01        | 1001        |
    public static long toLong(double n) {
        return BigDecimal.valueOf(n).movePointRight(2).longValue();
    }

    /// | long input | double output |
    /// | ---------- | ------------- |
    /// |    1       |  0.01         |
    /// |   10       |   0.1         |
    /// |  100       |   1.0         |
    /// | 1001       | 10.01         |
    public static double toDecimal(long n) {
        return BigDecimal.valueOf(n, 2).doubleValue();
    }
}
