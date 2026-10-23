package com.goaerion.numberscore;

import org.apache.commons.numbers.core.DD;
import org.apache.commons.numbers.core.DDMath;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DD / DDMath 测试：double-double 高精度算术（约 106 位有效数字）。
 */
class DDTest {

    @Test
    void ofCreatesFromPrimitive() {
        // 从 double/int/long 构造 double-double，高部为原值、低部为 0
        assertEquals(1.5, DD.of(1.5).doubleValue());
        assertEquals(2.0, DD.of(2).doubleValue());
        assertEquals(-3L, DD.of(-3L).longValue());
    }

    @Test
    void ofSumKeepsCancellationTerm() {
        // 普通 double：1e100 + 1 - 1e100 == 0.0，1 被吞掉
        double naive = 1e100 + 1 - 1e100;
        assertEquals(0.0, naive);
        // DD 保留低部误差：(hi, lo) = (1e100, 1)，相减后精确还原 1
        assertEquals(1.0, DD.of(1e100).add(1).subtract(DD.of(1e100)).doubleValue());
        assertEquals(3.0, DD.ofSum(1.0, 2.0).doubleValue());
    }

    @Test
    void basicArithmeticOperations() {
        // 四则运算：add/subtract/multiply/divide/reciprocal/pow，均支持 double 与 DD 两种入参
        assertEquals(5.0, DD.of(2).add(3).doubleValue());
        assertEquals(5.0, DD.of(2).add(DD.of(3)).doubleValue());
        assertEquals(2.0, DD.of(5).subtract(3).doubleValue());
        assertEquals(12.0, DD.of(3).multiply(4).doubleValue());
        assertEquals(12.0, DD.of(3).multiply(DD.of(4)).doubleValue());
        assertEquals(5.0, DD.of(10).divide(2).doubleValue());
        assertEquals(0.25, DD.of(4).reciprocal().doubleValue());
        assertEquals(0.5, DD.of(2).pow(-1).doubleValue());
        assertEquals(1.0, DD.of(2).pow(0).doubleValue());
        assertEquals(1024.0, DD.of(2).pow(10).doubleValue());
    }

    @Test
    void sqrtSquareRoot() {
        // 开方：sqrt(4) == 2；sqrt(2) * sqrt(2) ≈ 2，双精度下的舍入误差被消除
        assertEquals(2.0, DD.of(4).sqrt().doubleValue());
        assertEquals(2.0, DD.of(2).sqrt().multiply(DD.of(2).sqrt()).doubleValue(), 1e-15);
    }

    @Test
    void negateAndAbs() {
        // 取反与绝对值
        assertEquals(-2.0, DD.of(2).negate().doubleValue());
        assertEquals(2.0, DD.of(-2).abs().doubleValue());
        assertEquals(0.0, DD.of(0).abs().doubleValue());
    }

    @Test
    void floorAndCeil() {
        // 向下/向上取整（对高部取整，保留符号语义）
        assertEquals(2.0, DD.of(2.7).floor().doubleValue());
        assertEquals(3.0, DD.of(2.7).ceil().doubleValue());
        assertEquals(-3.0, DD.of(-2.7).floor().doubleValue());
        assertEquals(-2.0, DD.of(-2.7).ceil().doubleValue());
    }

    @Test
    void frexpSplitsFractionAndExponent() {
        // frexp：x = f * 2^exp，f 落在 [0.5, 1)，指数写入 exp[0]
        int[] exp = new int[1];
        DD fraction = DD.of(8).frexp(exp);
        assertEquals(8.0, fraction.doubleValue() * Math.pow(2, exp[0]));
    }

    @Test
    void isFiniteAndConversions() {
        // isFinite 判断与各数值类型转换；bigDecimalValue 为精确转换，非有限值抛 NumberFormatException
        assertTrue(DD.of(1.25).isFinite());
        assertFalse(DD.of(Double.POSITIVE_INFINITY).isFinite());
        assertEquals(1.25f, DD.of(1.25).floatValue());
        assertEquals(1, DD.of(1.25).intValue());
        assertEquals(new BigDecimal("1.5"), DD.of(1.5).bigDecimalValue());
        assertThrows(NumberFormatException.class,
                () -> DD.of(Double.POSITIVE_INFINITY).bigDecimalValue());
    }

    @Test
    void ddMathPowExtendedPrecision() {
        // DDMath.pow 扩展精度幂：结果以 (f + ff) * 2^exp 形式返回，指数写入 exp[0]
        long[] exp = new long[1];
        DD fraction = DDMath.pow(DD.of(2), 10, exp);
        double value = fraction.doubleValue() * Math.pow(2, exp[0]);
        assertEquals(1024.0, value, 1e-9);

        long[] exp2 = new long[1];
        DD fraction2 = DDMath.pow(DD.of(2), -3, exp2);
        assertEquals(0.125, fraction2.doubleValue() * Math.pow(2, exp2[0]), 1e-9);
    }
}
