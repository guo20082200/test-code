package com.goaerion.numberscore;

import org.apache.commons.numbers.core.Norm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Norm 测试：欧几里得/曼哈顿/切比雪夫范数，及防上溢/下溢缩放。
 */
class NormTest {

    @Test
    void euclideanNorm() {
        // 欧几里得范数：sqrt(a^2 + b^2 + ...)
        assertEquals(5.0, Norm.EUCLIDEAN.of(3, -4));
        assertEquals(1.0, Norm.EUCLIDEAN.of(new double[]{1}));
    }

    @Test
    void manhattanNorm() {
        // 曼哈顿范数：各分量绝对值之和
        assertEquals(12.0, Norm.MANHATTAN.of(3, -4, 5));
    }

    @Test
    void maximumNorm() {
        // 最大绝对值（切比雪夫范数）
        assertEquals(8.0, Norm.MAXIMUM.of(new double[]{3, -4, 5, -6, -7, -8}));
    }

    @Test
    void euclideanAvoidsOverflow() {
        // 大数场景：中间量会溢出，但 Norm 通过缩放避免，结果仍为有限值
        double big = Double.MAX_VALUE * 0.5;
        double length = Norm.EUCLIDEAN.of(big, big, big);
        double expected = Math.sqrt(0.5 * 0.5 * 3) * Double.MAX_VALUE;
        assertTrue(Double.isFinite(length), "结果应为有限值");
        assertEquals(expected, length, expected * 1e-12);
    }
}
