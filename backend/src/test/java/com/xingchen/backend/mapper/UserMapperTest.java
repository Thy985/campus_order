package com.xingchen.backend.mapper;

import com.xingchen.backend.entity.User;
import com.xingchen.backend.util.constant.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserMapper集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setAvatar("http://example.com/avatar.jpg");
        testUser.setGender(1);
        testUser.setUserType(Constants.UserType.NORMAL);
        testUser.setStatus(Constants.UserStatus.ENABLED);
        testUser.setLoginCount(0);
        testUser.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        testUser.setRegisterTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试用户插入操作")
    void testInsertUser() {
        int result = userMapper.insert(testUser);

        assertTrue(result > 0, "用户插入应该成功");
        assertNotNull(testUser.getId(), "插入后用户ID不应为空");

        User savedUser = userMapper.selectOneById(testUser.getId());
        assertNotNull(savedUser, "查询到的用户不应为空");
        assertEquals(testUser.getUsername(), savedUser.getUsername());
        assertEquals(testUser.getPhone(), savedUser.getPhone());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getNickname(), savedUser.getNickname());
    }

    @Test
    @DisplayName("测试用户更新操作")
    void testUpdateUser() {
        userMapper.insert(testUser);

        testUser.setNickname("更新后的昵称");
        testUser.setEmail("updated@example.com");
        testUser.setAvatar("http://example.com/new_avatar.jpg");
        testUser.setLoginCount(5);
        testUser.setLastLoginTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        int result = userMapper.update(testUser);

        assertTrue(result > 0, "用户更新应该成功");

        User updatedUser = userMapper.selectOneById(testUser.getId());
        assertEquals("更新后的昵称", updatedUser.getNickname());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("http://example.com/new_avatar.jpg", updatedUser.getAvatar());
        assertEquals(5, updatedUser.getLoginCount());
        assertNotNull(updatedUser.getLastLoginTime());
    }

    @Test
    @DisplayName("测试根据ID查询用户")
    void testSelectById() {
        userMapper.insert(testUser);
        Long userId = testUser.getId();

        User foundUser = userMapper.selectOneById(userId);

        assertNotNull(foundUser, "根据ID应能查询到用户");
        assertEquals(userId, foundUser.getId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getPhone(), foundUser.getPhone());
    }

    @Test
    @DisplayName("测试根据手机号查询用户")
    void testSelectByPhone() {
        userMapper.insert(testUser);

        Optional<User> foundUser = userMapper.selectByPhone("13800138000");

        assertTrue(foundUser.isPresent(), "根据手机号应能查询到用户");
        assertEquals(testUser.getPhone(), foundUser.get().getPhone());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
    }

    @Test
    @DisplayName("测试根据手机号查询用户-不存在")
    void testSelectByPhone_NotExist() {
        Optional<User> foundUser = userMapper.selectByPhone("13999999999");

        assertFalse(foundUser.isPresent(), "不存在的手机号应返回空Optional");
    }

    @Test
    @DisplayName("测试根据用户名查询用户")
    void testSelectByUsername() {
        userMapper.insert(testUser);

        Optional<User> foundUser = userMapper.selectByUsername("testuser");

        assertTrue(foundUser.isPresent(), "根据用户名应能查询到用户");
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        assertEquals(testUser.getPhone(), foundUser.get().getPhone());
    }

    @Test
    @DisplayName("测试根据用户名查询用户-不存在")
    void testSelectByUsername_NotExist() {
        Optional<User> foundUser = userMapper.selectByUsername("nonexistent");

        assertFalse(foundUser.isPresent(), "不存在的用户名应返回空Optional");
    }

    @Test
    @DisplayName("测试根据用户类型查询用户列表")
    void testSelectByUserType() {
        userMapper.insert(testUser);

        User merchantUser = new User();
        merchantUser.setUsername("merchantuser");
        merchantUser.setNickname("商家用户");
        merchantUser.setPhone("13900139000");
        merchantUser.setEmail("merchant@example.com");
        merchantUser.setPassword("password123");
        merchantUser.setUserType(Constants.UserType.MERCHANT);
        merchantUser.setMerchantId(1L);
        merchantUser.setStatus(Constants.UserStatus.ENABLED);
        merchantUser.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        merchantUser.setCreateTime(LocalDateTime.now());
        merchantUser.setUpdateTime(LocalDateTime.now());
        merchantUser.setRegisterTime(LocalDateTime.now());
        userMapper.insert(merchantUser);

        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setNickname("管理员");
        adminUser.setPhone("13700137000");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password123");
        adminUser.setUserType(Constants.UserType.ADMIN);
        adminUser.setStatus(Constants.UserStatus.ENABLED);
        adminUser.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        adminUser.setCreateTime(LocalDateTime.now());
        adminUser.setUpdateTime(LocalDateTime.now());
        adminUser.setRegisterTime(LocalDateTime.now());
        userMapper.insert(adminUser);

        List<User> normalUsers = userMapper.selectByUserType(Constants.UserType.NORMAL, 10, 0);
        List<User> merchantUsers = userMapper.selectByUserType(Constants.UserType.MERCHANT, 10, 0);
        List<User> adminUsers = userMapper.selectByUserType(Constants.UserType.ADMIN, 10, 0);

        assertNotNull(normalUsers);
        assertTrue(normalUsers.stream().allMatch(u -> u.getUserType().equals(Constants.UserType.NORMAL)),
                "所有普通用户应具有NORMAL类型");

        assertNotNull(merchantUsers);
        assertTrue(merchantUsers.stream().allMatch(u -> u.getUserType().equals(Constants.UserType.MERCHANT)),
                "所有商家用户应具有MERCHANT类型");

        assertNotNull(adminUsers);
        assertTrue(adminUsers.stream().allMatch(u -> u.getUserType().equals(Constants.UserType.ADMIN)),
                "所有管理员应具有ADMIN类型");
    }

    @Test
    @DisplayName("测试用户列表查询-分页")
    void testSelectUserListWithPagination() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setUsername("testuser" + i);
            user.setNickname("测试用户" + i);
            user.setPhone("13900000" + String.format("%03d", i));
            user.setEmail("test" + i + "@example.com");
            user.setPassword("password123");
            user.setUserType(Constants.UserType.NORMAL);
            user.setStatus(Constants.UserStatus.ENABLED);
            user.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setRegisterTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        List<User> page1 = userMapper.selectByUserType(Constants.UserType.NORMAL, 2, 0);
        List<User> page2 = userMapper.selectByUserType(Constants.UserType.NORMAL, 2, 2);

        assertNotNull(page1);
        assertNotNull(page2);
        assertEquals(2, page1.size(), "第一页应返回2条记录");
        assertEquals(2, page2.size(), "第二页应返回2条记录");
    }

    @Test
    @DisplayName("测试更新用户状态")
    void testUpdateUserStatus() {
        userMapper.insert(testUser);

        testUser.setStatus(Constants.UserStatus.DISABLED);
        userMapper.update(testUser);

        User updatedUser = userMapper.selectOneById(testUser.getId());
        assertEquals(Constants.UserStatus.DISABLED, updatedUser.getStatus());
    }

    @Test
    @DisplayName("测试更新用户密码")
    void testUpdateUserPassword() {
        userMapper.insert(testUser);

        testUser.setPassword("newpassword456");
        testUser.setPasswordModifyTime(LocalDateTime.now());
        userMapper.update(testUser);

        User updatedUser = userMapper.selectOneById(testUser.getId());
        assertEquals("newpassword456", updatedUser.getPassword());
        assertNotNull(updatedUser.getPasswordModifyTime());
    }

    @Test
    @DisplayName("测试逻辑删除用户")
    void testDeleteUser() {
        userMapper.insert(testUser);

        testUser.setIsDeleted(Constants.DeleteFlag.DELETED);
        userMapper.update(testUser);

        User updatedUser = userMapper.selectOneById(testUser.getId());
        assertEquals(Constants.DeleteFlag.DELETED, updatedUser.getIsDeleted());
    }

    @Test
    @DisplayName("测试更新用户登录信息")
    void testUpdateLoginInfo() {
        userMapper.insert(testUser);

        testUser.setLoginCount(10);
        testUser.setLastLoginTime(LocalDateTime.now());
        testUser.setLastLoginIp("192.168.1.1");
        userMapper.update(testUser);

        User updatedUser = userMapper.selectOneById(testUser.getId());
        assertEquals(10, updatedUser.getLoginCount());
        assertNotNull(updatedUser.getLastLoginTime());
        assertEquals("192.168.1.1", updatedUser.getLastLoginIp());
    }

    @Test
    @DisplayName("测试用户唯一性-手机号")
    void testPhoneUniqueness() {
        userMapper.insert(testUser);

        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setNickname("另一个用户");
        anotherUser.setPhone("13800138000");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password123");
        anotherUser.setUserType(Constants.UserType.NORMAL);
        anotherUser.setStatus(Constants.UserStatus.ENABLED);
        anotherUser.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        anotherUser.setCreateTime(LocalDateTime.now());
        anotherUser.setUpdateTime(LocalDateTime.now());
        anotherUser.setRegisterTime(LocalDateTime.now());

        assertThrows(Exception.class, () -> userMapper.insert(anotherUser),
                "重复的手机号应该抛出异常");
    }
}
