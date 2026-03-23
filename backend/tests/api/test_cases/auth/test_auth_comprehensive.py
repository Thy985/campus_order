"""
Comprehensive Authentication Service Tests
认证服务综合测试

Tests for user authentication endpoints including:
- User registration
- User login
- Verification code
- Forgot password
- Token refresh
- Logout
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestAuthComprehensive:
    """Comprehensive authentication service test suite"""

    @pytest.fixture(autouse=True)
    def setup(self, fresh_http_client: HTTPClient, data_generator: DataGenerator):
        """Setup test fixtures"""
        self.http_client = fresh_http_client
        self.data_generator = data_generator
        self.assertions = APIAssertions()
        self.validator = ResponseValidator()
        self.field_validator = FieldValidator()

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        # Validate HTTP status code
        self.assertions.assert_success(response)

        # Validate required fields
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

        # Validate response code (business logic)
        if "code" in required:
            code = response.body.get("code")
            assert code == 200, f"Expected business code 200, got {code}"

    def _validate_error_response(self, response: APIResponse, expected_status: int = None):
        """Helper method to validate error API response"""
        if expected_status:
            self.assertions.assert_status_code(response, expected_status)
        self.assertions.assert_has_field(response, "code")
        self.assertions.assert_has_field(response, "message")

    # ==================== Registration Tests ====================

    def test_register_success(self):
        """Test successful user registration"""
        user_data = self.data_generator.generate_user()

        response = self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate response data structure
        data = response.body.get("data", {})
        assert "userId" in data or "id" in data, "Response should contain user ID"
        assert "token" in data or "accessToken" in data, "Response should contain token"

    def test_register_duplicate_phone(self):
        """Test registration with duplicate phone number"""
        # First registration
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        # Second registration with same phone
        response = self.http_client.post("/api/auth/register", json_data={
            "username": self.data_generator.generate_username(),
            "phone": user_data["phone"],
            "password": self.data_generator.generate_password(),
            "verifyCode": "123456"
        })

        self._validate_error_response(response)
        assert response.body.get("code") != 200, "Should return error for duplicate phone"

    def test_register_invalid_phone(self):
        """Test registration with invalid phone number"""
        response = self.http_client.post("/api/auth/register", json_data={
            "username": self.data_generator.generate_username(),
            "phone": "12345678901",  # Invalid phone
            "password": self.data_generator.generate_password(),
            "verifyCode": "123456"
        })

        self._validate_error_response(response)
        assert response.status_code in [400, 422], "Should return 400 or 422 for invalid phone"

    def test_register_weak_password(self):
        """Test registration with weak password"""
        response = self.http_client.post("/api/auth/register", json_data={
            "username": self.data_generator.generate_username(),
            "phone": self.data_generator.generate_phone(),
            "password": "123",  # Weak password
            "verifyCode": "123456"
        })

        self._validate_error_response(response)
        assert response.status_code in [400, 422], "Should return 400 or 422 for weak password"

    def test_register_missing_required_fields(self):
        """Test registration with missing required fields"""
        response = self.http_client.post("/api/auth/register", json_data={
            "username": self.data_generator.generate_username()
            # Missing phone, password, verifyCode
        })

        self._validate_error_response(response)
        assert response.status_code == 400, "Should return 400 for missing fields"

    # ==================== Login Tests ====================

    def test_login_success_by_phone(self):
        """Test successful login with phone number"""
        # First register a user
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        # Then login
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate token and user info
        data = response.body.get("data", {})
        assert "token" in data or "accessToken" in data, "Response should contain token"
        assert "userId" in data or "id" in data, "Response should contain user ID"

    def test_login_invalid_credentials(self):
        """Test login with invalid credentials"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.data_generator.generate_phone(),
            "password": "wrong_password"
        })

        self._validate_error_response(response, 401)
        assert response.body.get("code") != 200, "Should return error code"

    def test_login_nonexistent_user(self):
        """Test login with non-existent user"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": "13800000000",
            "password": "some_password"
        })

        self._validate_error_response(response, 401)

    def test_login_missing_fields(self):
        """Test login with missing fields"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.data_generator.generate_phone()
            # Missing password
        })

        self._validate_error_response(response)
        assert response.status_code == 400, "Should return 400 for missing fields"

    # ==================== Verification Code Tests ====================

    def test_send_verify_code_success(self):
        """Test sending verification code successfully"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "phone": self.data_generator.generate_phone(),
            "type": "register"
        })

        # May return success or rate limit
        assert response.status_code in [200, 429], "Should return 200 or 429"
        self.assertions.assert_has_field(response, "code")
        self.assertions.assert_has_field(response, "message")

    def test_send_verify_code_invalid_phone(self):
        """Test sending verification code to invalid phone"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "phone": "12345678901",  # Invalid phone
            "type": "register"
        })

        self._validate_error_response(response)
        assert response.status_code in [400, 422], "Should return 400 or 422"

    def test_send_verify_code_missing_phone(self):
        """Test sending verification code without phone"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "type": "register"
        })

        self._validate_error_response(response, 400)

    # ==================== Forgot Password Tests ====================

    def test_forgot_password_success(self):
        """Test forgot password with valid phone and code"""
        # First register a user
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        # Request password reset
        response = self.http_client.post("/api/auth/forgot-password", json_data={
            "phone": user_data["phone"],
            "verifyCode": "123456",
            "newPassword": self.data_generator.generate_password()
        })

        # May succeed or require additional verification
        assert response.status_code in [200, 400, 422]
        self.assertions.assert_has_field(response, "code")
        self.assertions.assert_has_field(response, "message")

    def test_forgot_password_invalid_code(self):
        """Test forgot password with invalid verification code"""
        response = self.http_client.post("/api/auth/forgot-password", json_data={
            "phone": self.data_generator.generate_phone(),
            "verifyCode": "000000",  # Invalid code
            "newPassword": self.data_generator.generate_password()
        })

        self._validate_error_response(response)
        assert response.body.get("code") != 200, "Should return error for invalid code"

    def test_forgot_password_weak_new_password(self):
        """Test forgot password with weak new password"""
        response = self.http_client.post("/api/auth/forgot-password", json_data={
            "phone": self.data_generator.generate_phone(),
            "verifyCode": "123456",
            "newPassword": "123"  # Weak password
        })

        self._validate_error_response(response)
        assert response.status_code in [400, 422], "Should return 400 or 422"

    # ==================== Token Refresh Tests ====================

    def test_refresh_token_success(self):
        """Test refreshing access token"""
        # First register and login
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        login_response = self.http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        refresh_token = login_response.body.get("data", {}).get("refreshToken")
        if refresh_token:
            response = self.http_client.post("/api/auth/refresh-token", json_data={
                "refreshToken": refresh_token
            })

            self._validate_success_response(response, ["code", "message", "data"])
            data = response.body.get("data", {})
            assert "token" in data or "accessToken" in data, "Should return new access token"

    def test_refresh_token_invalid(self):
        """Test refreshing with invalid token"""
        response = self.http_client.post("/api/auth/refresh-token", json_data={
            "refreshToken": "invalid_token"
        })

        self._validate_error_response(response)
        assert response.status_code in [401, 403], "Should return 401 or 403"

    # ==================== Logout Tests ====================

    def test_logout_success(self):
        """Test successful logout"""
        # First register and login
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        login_response = self.http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        token = login_response.body.get("data", {}).get("token") or \
                login_response.body.get("data", {}).get("accessToken")

        if token:
            self.http_client.set_token(token)
            response = self.http_client.post("/api/auth/logout")

            self._validate_success_response(response)

    def test_logout_without_token(self):
        """Test logout without authentication token"""
        response = self.http_client.post("/api/auth/logout")

        # Should return 401 or handle gracefully
        assert response.status_code in [200, 401], "Should return 200 or 401"

    # ==================== Performance Tests ====================

    def test_login_response_time(self):
        """Test login response time is within acceptable limit"""
        user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        response = self.http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        self.assertions.assert_response_time(response, 2.0)

    def test_register_response_time(self):
        """Test registration response time is within acceptable limit"""
        user_data = self.data_generator.generate_user()

        response = self.http_client.post("/api/auth/register", json_data={
            "username": user_data["username"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        self.assertions.assert_response_time(response, 3.0)
