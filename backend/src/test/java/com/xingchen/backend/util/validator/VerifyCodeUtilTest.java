package com.xingchen.backend.util.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证码工具类测试
 *
 * @author 小跃
 * @date 2026-03-14
 */
class VerifyCodeUtilTest {

    // ==================== generateCode 测试 ====================

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 6, 8, 10})
    @DisplayName("验证生成数字验证码长度")
    void testGenerateNumericCode_Length(int length) {
        String code = VerifyCodeUtil.generateNumericCode(length);

        assertNotNull(code);
        assertEquals(length, code.length());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 6, 8, 10})
    @DisplayName("验证生成混合验证码长度")
    void testGenerateMixedCode_Length(int length) {
        String code = VerifyCodeUtil.generateMixedCode(length);

        assertNotNull(code);
        assertEquals(length, code.length());
    }

    @Test
    @DisplayName("验证数字验证码只包含数字")
    void testGenerateNumericCode_Content() {
        String code = VerifyCodeUtil.generateNumericCode(10);

        assertTrue(code.matches("\\d+"));
    }

    @Test
    @DisplayName("验证混合验证码包含字母和数字")
    void testGenerateMixedCode_Content() {
        String code = VerifyCodeUtil.generateMixedCode(100);

        // 应该包含字母
        assertTrue(code.matches(".*[a-zA-Z].*"));
        // 应该包含数字
        assertTrue(code.matches(".*\\d.*"));
    }

    @Test
    @DisplayName("验证验证码唯一性")
    void testGenerateCode_Uniqueness() {
        String code1 = VerifyCodeUtil.generateNumericCode(6);
        String code2 = VerifyCodeUtil.generateNumericCode(6);

        // 两次生成的验证码应该不同（概率极低相同）
        assertNotEquals(code1, code2);
    }

    @Test
    @DisplayName("验证混合验证码字符集")
    void testGenerateMixedCode_Charset() {
        String code = VerifyCodeUtil.generateMixedCode(1000);

        // 验证只包含允许的字符
        assertTrue(code.matches("[0-9a-zA-Z]+"));

        // 验证不包含特殊字符
        assertFalse(code.matches(".*[!@#$%^&*()].*"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("验证生成验证码边界值")
    void testGenerateCode_Boundary(int length) {
        // 0或负数长度应该返回空字符串
        String numericCode = VerifyCodeUtil.generateNumericCode(length);
        String mixedCode = VerifyCodeUtil.generateMixedCode(length);

        // 根据实现，可能返回空字符串或抛出异常
        assertNotNull(numericCode);
        assertNotNull(mixedCode);
    }

    // ==================== validateCode 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "123456, 123456",
        "abc123, abc123",
        "ABC123, ABC123",
        "000000, 000000",
        "999999, 999999"
    })
    @DisplayName("验证正确验证码")
    void testValidateCode_Correct(String inputCode, String storedCode) {
        boolean result = VerifyCodeUtil.validateCode(inputCode, storedCode);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        "123456, 654321",
        "abc123, ABC123",
        "123456, 12345",
        "123456, 1234567",
        "000000, 000001"
    })
    @DisplayName("验证错误验证码")
    void testValidateCode_Wrong(String inputCode, String storedCode) {
        boolean result = VerifyCodeUtil.validateCode(inputCode, storedCode);

        assertFalse(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("验证空输入验证码")
    void testValidateCode_NullInput(String inputCode) {
        boolean result = VerifyCodeUtil.validateCode(inputCode, "123456");

        assertFalse(result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("验证空存储验证码")
    void testValidateCode_NullStored(String storedCode) {
        boolean result = VerifyCodeUtil.validateCode("123456", storedCode);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证验证码大小写敏感")
    void testValidateCode_CaseSensitive() {
        boolean result1 = VerifyCodeUtil.validateCode("ABC123", "abc123");
        boolean result2 = VerifyCodeUtil.validateCode("abc123", "ABC123");

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    @DisplayName("验证验证码空格敏感")
    void testValidateCode_SpaceSensitive() {
        boolean result1 = VerifyCodeUtil.validateCode("123456", "123 456");
        boolean result2 = VerifyCodeUtil.validateCode("123 456", "123456");

        assertFalse(result1);
        assertFalse(result2);
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("验证生成并校验流程")
    void testGenerateAndValidate() {
        // 生成验证码
        String code = VerifyCodeUtil.generateNumericCode(6);

        // 正确校验
        assertTrue(VerifyCodeUtil.validateCode(code, code));

        // 错误校验
        String wrongCode = code.equals("000000") ? "000001" : "000000";
        assertFalse(VerifyCodeUtil.validateCode(wrongCode, code));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 6, 8})
    @DisplayName("验证不同长度验证码生成和校验")
    void testGenerateAndValidate_DifferentLengths(int length) {
        String numericCode = VerifyCodeUtil.generateNumericCode(length);
        String mixedCode = VerifyCodeUtil.generateMixedCode(length);

        assertEquals(length, numericCode.length());
        assertEquals(length, mixedCode.length());

        assertTrue(VerifyCodeUtil.validateCode(numericCode, numericCode));
        assertTrue(VerifyCodeUtil.validateCode(mixedCode, mixedCode));
    }

    @Test
    @DisplayName("验证数字验证码范围")
    void testNumericCode_Range() {
        // 生成大量验证码验证范围
        for (int i = 0; i < 100; i++) {
            String code = VerifyCodeUtil.generateNumericCode(6);
            int value = Integer.parseInt(code);
            assertTrue(value >= 0 && value <= 999999);
        }
    }

    @Test
    @DisplayName("验证验证码长度一致性")
    void testCodeLength_Consistency() {
        // 多次生成验证长度一致性
        for (int i = 0; i < 50; i++) {
            String code = VerifyCodeUtil.generateNumericCode(6);
            assertEquals(6, code.length());
        }
    }
}
