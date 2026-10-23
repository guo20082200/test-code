package com.goaerion.numberscore;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Precision 测试：浮点数比较（绝对/相对/ULP 容差）、compareTo、round、representableDelta 与 DoubleEquivalence。
 */
class PrecisionTest {

    @Test
    void defaultEqualsDisallowsNumbersBetween() {
        // 默认 equals 不允许 a、b 之间有可表示数，等价于 equals(a, b, 1)
        assertTrue(Precision.equals(1000.0, 1000.0));
        // 相差 1 个 ULP 仍视为相等
        assertTrue(Precision.equals(1000.0, 1000.0 + Math.ulp(1000.0)));
        assertFalse(Precision.equals(1000.0, 1000.0 + 2 * Math.ulp(1000.0)));
    }

    @Test
    void equalsWithAbsoluteTolerance() {
        // 绝对容差：|a - b| <= eps 视为相等
        assertTrue(Precision.equals(1000.0, 1001.0, 1.0));
        assertFalse(Precision.equals(1000.0, 1001.0, 0.5));
    }

    @Test
    void equalsWithUlpTolerance() {
        // ULP 容差：(n - 1) 个数在 a、b 之间视为相等
        assertTrue(Precision.equals(1000.0, 1000.0 + 2 * Math.ulp(1000.0), 2));
        assertFalse(Precision.equals(1000.0, 1000.0 + 3 * Math.ulp(1000.0), 2));
    }

    @Test
    void relativeToleranceComparison() {
        // 相对容差：按输入中绝对值较大的数归一化计算差值
        assertFalse(Precision.equalsWithRelativeTolerance(1000.0, 1001.0, 1e-6));
        assertTrue(Precision.equalsWithRelativeTolerance(1000.0, 1001.0, 1e-3));
    }

    @Test
    void equalsHandlesNaNAndInfinity() {
        // NaN 默认不相等，equalsIncludingNaN 才视为相等；同号无穷视为相等
        assertFalse(Precision.equals(Double.NaN, 1000.0));
        assertFalse(Precision.equals(Double.NaN, Double.NaN));
        assertTrue(Precision.equalsIncludingNaN(Double.NaN, Double.NaN));
        assertTrue(Precision.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse(Precision.equals(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    @Test
    void compareToOrdersWithinTolerance() {
        // compareTo：容差内视为相等返回 0，否则按大小返回 -1/1
        assertEquals(0, Precision.compareTo(100, 100, 0.0));
        assertEquals(0, Precision.compareTo(100, 101, 1.0));
        assertEquals(-1, Precision.compareTo(100, 102, 1.0));
        assertEquals(1, Precision.compareTo(102, 100, 1.0));
    }

    @Test
    void roundHalfUp() {
        // 四舍五入，scale 可为负数（按十/百位取整）
        assertEquals(678.13, Precision.round(678.125, 2));
        assertEquals(678.1, Precision.round(678.125, 1));
        assertEquals(678.0, Precision.round(678.125, 0));
        assertEquals(680.0, Precision.round(678.125, -1));
        assertEquals(700.0, Precision.round(678.125, -2));
    }

    @Test
    void representableDeltaReturnsNearestRepresentable() {
        // 返回最接近理想增量 delta 的、与 x 同数量级的可表示 double
        assertEquals(0.10000000000000009, Precision.representableDelta(1.0, 0.1));
    }

    @Test
    void doubleEquivalenceOfEpsilonComparison() {
        // 函数式比较器：可作流式比较/排序使用
        Precision.DoubleEquivalence eq = Precision.doubleEquivalenceOfEpsilon(1.0);
        assertTrue(eq.eq(1000.0, 1001.0));
        assertFalse(eq.eq(1000.0, 1002.0));
        assertEquals(0, eq.compare(1000.0, 1001.0));
        assertEquals(-1, eq.compare(100.0, 102.0));
        // 容差内视为相等：gt/lt 只在差值超出 epsilon 时严格成立
        assertTrue(eq.gt(1002.0, 1000.0));
        assertTrue(eq.lt(998.0, 1000.0));
        assertFalse(eq.gt(1001.0, 1000.0));
        assertFalse(eq.lt(999.0, 1000.0));
        // gte/lte 包含"视为相等"的情况
        assertTrue(eq.gte(1000.0, 1001.0));
        assertTrue(eq.lte(1001.0, 1000.0));
        assertTrue(eq.eqZero(0.0));
        assertEquals(1.0, eq.signum(10.0));
        assertEquals(-1.0, eq.signum(-10.0));
    }
}
