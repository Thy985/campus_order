package com.xingchen.backend.util.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类测试
 *
 * @author 小跃
 * @date 2026-03-14
 */
class PasswordUtilTest {

    // ==================== encode 测试 ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "123456",
        "password",
        "MyP@ssw0rd!",
        "admin123",
        "qwertyuiop"
    })
    @DisplayName("验证正常密码加密")
    void testEncryptPassword_Normal(String rawPassword) {
        String encrypted = PasswordUtil.encryptPassword(rawPassword);

        assertNotNull(encrypted);
        assertNotEquals(rawPassword, encrypted);
        // MD5加密结果应为32位十六进制字符串
        assertEquals(32, encrypted.length());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("验证空密码加密")
    void testEncryptPassword_NullOrEmpty(String rawPassword) {
        String encrypted = PasswordUtil.encryptPassword(rawPassword);

        // 空值也应该返回MD5结果
        assertNotNull(encrypted);
        assertEquals(32, encrypted.length());
    }

    @Test
    @DisplayName("验证相同密码产生相同加密结果")
    void testEncryptPassword_Consistency() {
        String password = "testPassword123";
        String encrypted1 = PasswordUtil.encryptPassword(password);
        String encrypted2 = PasswordUtil.encryptPassword(password);

        assertEquals(encrypted1, encrypted2);
    }

    @Test
    @DisplayName("验证不同密码产生不同加密结果")
    void testEncryptPassword_Uniqueness() {
        String password1 = "password123";
        String password2 = "password124";

        String encrypted1 = PasswordUtil.encryptPassword(password1);
        String encrypted2 = PasswordUtil.encryptPassword(password2);

        assertNotEquals(encrypted1, encrypted2);
    }

    // ==================== matches 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "123456, 123456",
        "password, password",
        "MyP@ssw0rd!, MyP@ssw0rd!",
        "admin, admin"
    })
    @DisplayName("验证正确密码匹配")
    void testValidatePassword_Correct(String rawPassword, String inputPassword) {
        String encryptedPassword = PasswordUtil.encryptPassword(rawPassword);
        boolean result = PasswordUtil.validatePassword(inputPassword, encryptedPassword);

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        "123456, 654321",
        "password, Password",
        "test, test123",
        "hello, world"
    })
    @DisplayName("验证错误密码匹配")
    void testValidatePassword_Incorrect(String rawPassword, String wrongPassword) {
        String encryptedPassword = PasswordUtil.encryptPassword(rawPassword);
        boolean result = PasswordUtil.validatePassword(wrongPassword, encryptedPassword);

        assertFalse(result);
    }

    @Test
    @DisplayName("验证带空格密码不匹配")
    void testValidatePassword_SpaceDifference() {
        String rawPassword = "admin";
        String wrongPassword = "admin "; // 带尾部空格
        String encryptedPassword = PasswordUtil.encryptPassword(rawPassword);

        boolean result = PasswordUtil.validatePassword(wrongPassword, encryptedPassword);
        assertFalse(result);
    }

    @Test
    @DisplayName("验证空密码匹配")
    void testValidatePassword_Empty() {
        String encryptedPassword = PasswordUtil.encryptPassword("123456");

        boolean result1 = PasswordUtil.validatePassword("", encryptedPassword);
        boolean result2 = PasswordUtil.validatePassword(null, encryptedPassword);

        assertFalse(result1);
        assertFalse(result2);
    }

    @Test
    @DisplayName("验证空加密密码匹配")
    void testValidatePassword_EmptyEncoded() {
        boolean result1 = PasswordUtil.validatePassword("123456", "");
        boolean result2 = PasswordUtil.validatePassword("123456", null);

        assertFalse(result1);
        assertFalse(result2);
    }

    // ==================== isStrong 测试 ====================

    @ParameterizedTest
    @CsvSource({
        "12345678a!, 3",     // 数字+小写字母+特殊字符，长度>8
        "Password1@, 3",     // 大写字母+小写字母+数字+特殊字符
        "Abc123!@#, 3",      // 强密码
        "MyStr0ng!, 3"       // 强密码
    })
    @DisplayName("验证强密码")
    void testCheckPasswordStrength_Strong(String password, int expectedStrength) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(expectedStrength, strength);
    }

    @ParameterizedTest
    @CsvSource({
        "123, 1",            // 太短
        "12345, 1",          // 太短
        "abcde, 1"           // 太短
    })
    @DisplayName("验证弱密码 - 太短")
    void testCheckPasswordStrength_Weak_TooShort(String password, int expectedStrength) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(expectedStrength, strength);
    }

    @ParameterizedTest
    @CsvSource({
        "123456, 2",         // 只有数字，长度>=6
        "1234567, 2",        // 只有数字，长度>=6
        "abcdef, 2",         // 只有字母，长度>=6
        "password, 2"        // 只有字母，长度>=6
    })
    @DisplayName("验证中等密码 - 单一类型")
    void testCheckPasswordStrength_Medium_SingleType(String password, int expectedStrength) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(expectedStrength, strength);
    }

    @ParameterizedTest
    @CsvSource({
        "123456a, 3",        // 数字+字母
        "123456A, 3",        // 数字+大写字母
        "password1, 3",      // 字母+数字
        "123456!, 3",        // 数字+特殊字符
        "passw0rd, 3"        // 字母+数字，长度>8
    })
    @DisplayName("验证中等密码 - 两种类型组合")
    void testCheckPasswordStrength_Medium_TwoTypes(String password, int expectedStrength) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(expectedStrength, strength);
    }

    @ParameterizedTest
    @CsvSource({
        "123456a!, 3",       // 数字+字母+特殊字符
        "Password1, 3",      // 大写+小写+数字
        "Passw0rd!, 3",      // 大写+小写+数字+特殊字符
        "A1b2C3d4!, 3"       // 混合类型，长度>8
    })
    @DisplayName("验证强密码 - 多种类型组合")
    void testCheckPasswordStrength_Strong_MultipleTypes(String password, int expectedStrength) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(expectedStrength, strength);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("验证空密码强度")
    void testCheckPasswordStrength_NullOrEmpty(String password) {
        int strength = PasswordUtil.checkPasswordStrength(password);
        assertEquals(1, strength);
    }

    @Test
    @DisplayName("验证密码强度上限")
    void testCheckPasswordStrength_MaxStrength() {
        // 即使满足所有条件，强度也不应超过3
        String veryStrongPassword = "Abc123!@#$%^&*()_+";
        int strength = PasswordUtil.checkPasswordStrength(veryStrongPassword);
        assertEquals(3, strength);
    }

    // ==================== 边界测试 ====================

    @Test
    @DisplayName("验证超长密码加密")
    void testEncryptPassword_LongPassword() {
        String password32 = "12345678901234567890123456789012";  // 32位
        String password33 = "123456789012345678901234567890123"; // 33位
        String password100 = "a".repeat(100);                     // 100位

        String encrypted1 = PasswordUtil.encryptPassword(password32);
        String encrypted2 = PasswordUtil.encryptPassword(password33);
        String encrypted3 = PasswordUtil.encryptPassword(password100);

        assertNotNull(encrypted1);
        assertNotNull(encrypted2);
        assertNotNull(encrypted3);
        assertEquals(32, encrypted1.length());
        assertEquals(32, encrypted2.length());
        assertEquals(32, encrypted3.length());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "!@#$%^&*()",
        "中文密码测试",
        "🔐🔑🔒",
        "\t\n\r"
    })
    @DisplayName("验证特殊字符密码加密")
    void testEncryptPassword_SpecialChars(String password) {
        String encrypted = PasswordUtil.encryptPassword(password);
        assertNotNull(encrypted);
        assertEquals(32, encrypted.length());

        // 验证可以正确匹配
        boolean match = PasswordUtil.validatePassword(password, encrypted);
        assertTrue(match);
    }
}
