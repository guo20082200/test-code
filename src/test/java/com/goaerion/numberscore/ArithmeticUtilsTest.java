package com.goaerion.numberscore;

import org.apache.commons.numbers.core.ArithmeticUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ArithmeticUtils 测试：最大公约数、最小公倍数、整数幂、无符号除法等。
 */
class ArithmeticUtilsTest {

    @Test
    void gcdGreatestCommonDivisor() {
        // 最大公约数：结果永不为负；gcd(0, 0) 特殊返回 0
        assertEquals(6, ArithmeticUtils.gcd(12, 18));
        assertEquals(6, ArithmeticUtils.gcd(-12, 18));
        assertEquals(5, ArithmeticUtils.gcd(0, 5));
        assertEquals(0, ArithmeticUtils.gcd(0, 0));
        assertEquals(18L, ArithmeticUtils.gcd(461952L, 116298L));
        // 结果为 2^31 超出 int 范围时抛 ArithmeticException
        assertThrows(ArithmeticException.class,
                () -> ArithmeticUtils.gcd(Integer.MIN_VALUE, 0));
    }

    @Test
    void lcmLeastCommonMultiple() {
        // 最小公倍数：结果永不为负；任一为 0 则结果为 0
        assertEquals(12, ArithmeticUtils.lcm(4, 6));
        assertEquals(12, ArithmeticUtils.lcm(-4, 6));
        assertEquals(0, ArithmeticUtils.lcm(0, 5));
        assertEquals(60L, ArithmeticUtils.lcm(12L, 5L));
    }

    @Test
    void powIntegerPower() {
        // 整数幂：0^0 == 1，指数为负抛 IllegalArgumentException
        assertEquals(1024, ArithmeticUtils.pow(2, 10));
        assertEquals(1, ArithmeticUtils.pow(0, 0));
        assertEquals(1, ArithmeticUtils.pow(2, 0));
        assertEquals(-1, ArithmeticUtils.pow(-1, 3));
        assertEquals(8L, ArithmeticUtils.pow(2L, 3));
        assertEquals(new BigInteger("1024"), ArithmeticUtils.pow(BigInteger.TWO, 10));
        assertThrows(IllegalArgumentException.class,
                () -> ArithmeticUtils.pow(2, -1));
    }

    @Test
    void isPowerOfTwoCheck() {
        // 判断是否为 2 的幂；0 返回 false
        assertTrue(ArithmeticUtils.isPowerOfTwo(1024L));
        assertTrue(ArithmeticUtils.isPowerOfTwo(1L));
        assertFalse(ArithmeticUtils.isPowerOfTwo(0L));
        assertFalse(ArithmeticUtils.isPowerOfTwo(1000L));
    }

    @Test
    void unsignedDivisionAndRemainder() {
        // 无符号除法与余数：按无符号语义解释操作数
        assertEquals(4, ArithmeticUtils.divideUnsigned(14, 3));
        assertEquals(2, ArithmeticUtils.remainderUnsigned(14, 3));
        assertEquals(4L, ArithmeticUtils.divideUnsigned(14L, 3L));
        assertEquals(2L, ArithmeticUtils.remainderUnsigned(14L, 3L));
        // -1 视为无符号 0xFFFFFFFF（= 4294967295），除以 2 得到 2147483647
        assertEquals(Integer.MAX_VALUE, ArithmeticUtils.divideUnsigned(-1, 2));
    }
}
