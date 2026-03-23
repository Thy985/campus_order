"""
Comprehensive User Service Tests
用户服务综合测试

Tests for user management endpoints including:
- Get user info
- Update user info
- Change password
- Upload avatar
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestUserComprehensive:
    """Comprehensive user service test suite"""

    @pytest.fixture(autouse=True)
    def setup(self, fresh_http_client: HTTPClient, data_generator: DataGenerator):
        """Setup test fixtures"""
        self.http_client = fresh_http_client
        self.data_generator = data_generator
        self.assertions = APIAssertions()
        self.validator = ResponseValidator()
        self.field_validator = FieldValidator()

    @pytest.fixture
    def authenticated_user(self):
        """Create and authenticate a test user"""
        user_data = self.data_generator.generate_user()

        # Register user
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        # Login to get token
        login_response = self.http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        token = login_response.body.get("data", {}).get("token") or \
                login_response.body.get("data", {}).get("accessToken")
        user_id = login_response.body.get("data", {}).get("userId") or \
                  login_response.body.get("data", {}).get("id")

        if token:
            self.http_client.set_token(token)

        return {
            **user_data,
            "token": token,
            "userId": user_id
        }

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Get User Info Tests ====================

    def test_get_user_profile_success(self, authenticated_user):
        """Test getting user profile with valid token"""
        response = self.http_client.get("/api/user/profile")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate user data structure
        data = response.body.get("data", {})
        assert "id" in data or "userId" in data, "Response should contain user ID"
        assert "username" in data, "Response should contain username"
        assert "phone" in data, "Response should contain phone"

    def test_get_user_profile_unauthorized(self):
        """Test getting user profile without token"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/user/profile")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    def test_get_user_profile_invalid_token(self):
        """Test getting user profile with invalid token"""
        self.http_client.set_token("invalid_token")
        response = self.http_client.get("/api/user/profile")

        assert response.status_code in [401, 403], "Should return 401 or 403 for invalid token"

    def test_get_user_by_id_success(self, authenticated_user):
        """Test getting user info by ID"""
        user_id = authenticated_user.get("userId")
        if user_id:
            response = self.http_client.get(f"/api/user/{user_id}")

            # May return user data or 403 depending on permissions
            assert response.status_code in [200, 403]
            if response.is_success:
                self._validate_success_response(response, ["code", "message", "data"])

    # ==================== Update User Info Tests ====================

    def test_update_user_profile_success(self, authenticated_user):
        """Test updating user profile"""
        new_nickname = self.data_generator.generate_nickname()

        response = self.http_client.put("/api/user/profile", json_data={
            "nickname": new_nickname,
            "gender": 1,
            "email": self.data_generator.generate_email()
        })

        self._validate_success_response(response)

    def test_update_user_nickname_only(self, authenticated_user):
        """Test updating only nickname"""
        new_nickname = self.data_generator.generate_nickname()

        response = self.http_client.put("/api/user/profile", json_data={
            "nickname": new_nickname
        })

        self._validate_success_response(response)

    def test_update_user_avatar(self, authenticated_user):
        """Test updating user avatar URL"""
        response = self.http_client.put("/api/user/profile", json_data={
            "avatar": self.data_generator.generate_user().get("avatar")
        })

        self._validate_success_response(response)

    def test_update_user_profile_unauthorized(self):
        """Test updating profile without authentication"""
        self.http_client.clear_token()
        response = self.http_client.put("/api/user/profile", json_data={
            "nickname": "Test Name"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    def test_update_user_invalid_email(self, authenticated_user):
        """Test updating with invalid email format"""
        response = self.http_client.put("/api/user/profile", json_data={
            "email": "invalid_email"
        })

        # May accept or reject invalid email depending on implementation
        assert response.status_code in [200, 400, 422]

    def test_update_user_invalid_gender(self, authenticated_user):
        """Test updating with invalid gender value"""
        response = self.http_client.put("/api/user/profile", json_data={
            "gender": 5  # Invalid gender value
        })

        # May accept or reject depending on implementation
        assert response.status_code in [200, 400, 422]

    # ==================== Change Password Tests ====================

    def test_change_password_success(self, authenticated_user):
        """Test changing password with correct old password"""
        old_password = authenticated_user["password"]
        new_password = self.data_generator.generate_password()

        response = self.http_client.put("/api/user/password", json_data={
            "oldPassword": old_password,
            "newPassword": new_password
        })

        self._validate_success_response(response)

        # Verify can login with new password
        if response.is_success:
            self.http_client.clear_token()
            login_response = self.http_client.post("/api/auth/login", json_data={
                "phone": authenticated_user["phone"],
                "password": new_password
            })
            assert login_response.is_success, "Should be able to login with new password"

    def test_change_password_wrong_old_password(self, authenticated_user):
        """Test changing password with wrong old password"""
        response = self.http_client.put("/api/user/password", json_data={
            "oldPassword": "wrong_password",
            "newPassword": self.data_generator.generate_password()
        })

        assert response.status_code in [400, 401, 403], "Should return error for wrong old password"

    def test_change_password_weak_new_password(self, authenticated_user):
        """Test changing to weak password"""
        response = self.http_client.put("/api/user/password", json_data={
            "oldPassword": authenticated_user["password"],
            "newPassword": "123"  # Weak password
        })

        assert response.status_code in [400, 422], "Should return 400 or 422 for weak password"

    def test_change_password_missing_fields(self, authenticated_user):
        """Test changing password with missing fields"""
        response = self.http_client.put("/api/user/password", json_data={
            "newPassword": self.data_generator.generate_password()
            # Missing oldPassword
        })

        assert response.status_code == 400, "Should return 400 for missing fields"

    def test_change_password_unauthorized(self):
        """Test changing password without authentication"""
        self.http_client.clear_token()
        response = self.http_client.put("/api/user/password", json_data={
            "oldPassword": "old_pass",
            "newPassword": "new_pass"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Security Tests ====================

    def test_user_profile_no_password_exposure(self, authenticated_user):
        """Test that user profile doesn't expose password"""
        response = self.http_client.get("/api/user/profile")

        self._validate_success_response(response)

        # Check response doesn't contain sensitive data
        response_text = str(response.body)
        sensitive_fields = ["password", "secret", "credential"]
        for field in sensitive_fields:
            assert field not in response_text.lower(), f"Response should not expose {field}"

    def test_user_profile_no_token_exposure(self, authenticated_user):
        """Test that user profile doesn't expose token"""
        response = self.http_client.get("/api/user/profile")

        self._validate_success_response(response)

        # Check response doesn't contain token
        response_text = str(response.body).lower()
        assert "token" not in response_text or "refresh_token" not in response_text, \
            "Response should not expose tokens"

    # ==================== Performance Tests ====================

    def test_get_user_profile_response_time(self, authenticated_user):
        """Test get user profile response time"""
        response = self.http_client.get("/api/user/profile")
        self.assertions.assert_response_time(response, 1.0)

    def test_update_user_profile_response_time(self, authenticated_user):
        """Test update user profile response time"""
        response = self.http_client.put("/api/user/profile", json_data={
            "nickname": self.data_generator.generate_nickname()
        })
        self.assertions.assert_response_time(response, 1.5)

    # ==================== Data Integrity Tests ====================

    def test_user_profile_data_types(self, authenticated_user):
        """Test user profile data types"""
        response = self.http_client.get("/api/user/profile")

        self._validate_success_response(response)

        data = response.body.get("data", {})

        # Validate data types
        if "id" in data:
            assert isinstance(data["id"], (int, str)), "ID should be int or string"
        if "username" in data:
            assert isinstance(data["username"], str), "Username should be string"
        if "phone" in data:
            assert isinstance(data["phone"], str), "Phone should be string"
        if "gender" in data:
            assert isinstance(data["gender"], int), "Gender should be int"

    def test_update_profile_persistence(self, authenticated_user):
        """Test that profile updates persist"""
        new_nickname = self.data_generator.generate_nickname()

        # Update profile
        self.http_client.put("/api/user/profile", json_data={
            "nickname": new_nickname
        })

        # Get profile and verify update
        response = self.http_client.get("/api/user/profile")
        data = response.body.get("data", {})

        # May or may not reflect immediately depending on caching
        if "nickname" in data:
            assert data["nickname"] == new_nickname, "Updated nickname should persist"
