package com.goaerion.numberscore;

import org.apache.commons.numbers.core.Sum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sum 测试：高精度求和与线性组合（抵消误差场景）。
 */
class SumTest {

    @Test
    void ofSumsValues() {
        // 普通求和（varargs 工厂方法）
        assertEquals(6.0, Sum.of(1, 2, 3).getAsDouble());
        assertEquals(-4.0, Sum.of(1.5, -2.5, -3.0).getAsDouble());
    }

    @Test
    void createWithChainedAdd() {
        // 链式累加：create 起步，add 增加值、addProduct 增加乘积项
        double result = Sum.create()
                .add(1)
                .addProduct(3, 4)
                .getAsDouble();
        assertEquals(13.0, result);
    }

    @Test
    void ofProductsComputesDotProduct() {
        // 两个等长数组的点积：3 * 5 + 4 * 6 = 39
        double dot = Sum.ofProducts(new double[]{3, 4}, new double[]{5, 6}).getAsDouble();
        assertEquals(39.0, dot);
    }

    @Test
    void cancellationErrorCorrection() {
        // 大数抵消场景：普通 double 求和误差被吞掉（结果 0.0），Sum 精确得到 -1.0
        double naive = 1e100 + 1 - 2 - 1e100;
        double accurate = Sum.of(1e100, 1, -2, -1e100).getAsDouble();
        assertEquals(0.0, naive);
        assertEquals(-1.0, accurate);
    }

    @Test
    void subtractTwoSums() {
        // 两个 Sum 实例相减，同样避免抵消误差
        Sum s1 = Sum.of(1e100, 1);
        Sum s2 = Sum.of(2, 1e100);
        assertEquals(-1.0, s1.subtract(s2).getAsDouble());
    }

    @Test
    void nonFiniteFollowsIeee754() {
        // 非有限值遵循 IEEE754：任一输入为 NaN 则结果为 NaN，为 -inf 则结果为 -inf
        assertTrue(Double.isNaN(Sum.of(1, 2, Double.NaN).getAsDouble()));
        assertEquals(Double.NEGATIVE_INFINITY, Sum.of(1, 2, Double.NEGATIVE_INFINITY).getAsDouble());
    }
}
