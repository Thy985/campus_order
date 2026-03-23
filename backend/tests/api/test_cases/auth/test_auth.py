"""
Authentication API tests.
Tests user registration, login, and verification code APIs.
"""
import pytest

from utils.http_client import get_http_client
from utils.data_generator import get_data_generator
from utils.assertions import (
    assert_status_code, assert_success, assert_has_field,
    assert_field_equals, assert_error_message
)


@pytest.mark.auth
@pytest.mark.smoke
class TestAuthRegister:
    """Test user registration API"""

    @pytest.fixture(autouse=True)
    def setup(self):
        self.http_client = get_http_client()
        self.data_generator = get_data_generator()
        self.http_client.clear_token()

    def test_register_success(self):
        """Test successful user registration"""
        user_data = self.data_generator.generate_user()

        response = self.http_client.post("/api/auth/register", json_data={
            "nickname": user_data["nickname"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        assert_success(response)
        assert_has_field(response, "code")
        assert_has_field(response, "message")

    def test_register_duplicate_phone(self):
        """Test registration with duplicate phone number"""
        user_data = self.data_generator.generate_user()

        # First registration
        self.http_client.post("/api/auth/register", json_data={
            "nickname": user_data["nickname"],
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        # Second registration with same phone
        response = self.http_client.post("/api/auth/register", json_data={
            "nickname": self.data_generator.generate_nickname(),
            "phone": user_data["phone"],
            "password": user_data["password"],
            "verifyCode": "123456"
        })

        assert_status_code(response, 400)

    def test_register_invalid_phone(self):
        """Test registration with invalid phone number"""
        response = self.http_client.post("/api/auth/register", json_data={
            "nickname": self.data_generator.generate_nickname(),
            "phone": "invalid_phone",
            "password": self.data_generator.generate_password(),
            "verifyCode": "123456"
        })

        assert_status_code(response, 400)

    def test_register_weak_password(self):
        """Test registration with weak password"""
        response = self.http_client.post("/api/auth/register", json_data={
            "nickname": self.data_generator.generate_nickname(),
            "phone": self.data_generator.generate_phone(),
            "password": "123",
            "verifyCode": "123456"
        })

        assert_status_code(response, 400)

    def test_register_missing_required_fields(self):
        """Test registration with missing required fields"""
        response = self.http_client.post("/api/auth/register", json_data={
            "nickname": self.data_generator.generate_nickname()
            # Missing phone, password, verifyCode
        })

        assert_status_code(response, 400)


@pytest.mark.auth
@pytest.mark.smoke
class TestAuthLogin:
    """Test user login API"""

    @pytest.fixture(autouse=True)
    def setup(self):
        self.http_client = get_http_client()
        self.data_generator = get_data_generator()
        self.http_client.clear_token()

        # Create a test user
        self.user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "nickname": self.user_data["nickname"],
            "phone": self.user_data["phone"],
            "password": self.user_data["password"],
            "verifyCode": "123456"
        })

    def test_login_success(self):
        """Test successful login"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.user_data["phone"],
            "password": self.user_data["password"]
        })

        assert_success(response)
        assert_has_field(response, "data")
        assert_has_field(response.body.get("data", {}), "token")

    def test_login_wrong_password(self):
        """Test login with wrong password"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.user_data["phone"],
            "password": "wrong_password"
        })

        assert_status_code(response, 401)

    def test_login_nonexistent_user(self):
        """Test login with non-existent user"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.data_generator.generate_phone(),
            "password": self.data_generator.generate_password()
        })

        assert_status_code(response, 401)

    def test_login_missing_fields(self):
        """Test login with missing fields"""
        response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.user_data["phone"]
            # Missing password
        })

        assert_status_code(response, 400)


@pytest.mark.auth
class TestAuthVerifyCode:
    """Test verification code API"""

    @pytest.fixture(autouse=True)
    def setup(self):
        self.http_client = get_http_client()
        self.data_generator = get_data_generator()
        self.http_client.clear_token()

    def test_send_verify_code_success(self):
        """Test sending verification code"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "phone": self.data_generator.generate_phone(),
            "type": "register"
        })

        # Should return success or rate limit
        assert response.status_code in [200, 429]

    def test_send_verify_code_invalid_phone(self):
        """Test sending verification code to invalid phone"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "phone": "invalid_phone",
            "type": "register"
        })

        assert_status_code(response, 400)

    def test_send_verify_code_missing_type(self):
        """Test sending verification code without type"""
        response = self.http_client.post("/api/auth/send-verify-code", json_data={
            "phone": self.data_generator.generate_phone()
        })

        assert_status_code(response, 400)


@pytest.mark.auth
class TestAuthLogout:
    """Test logout API"""

    @pytest.fixture(autouse=True)
    def setup(self):
        self.http_client = get_http_client()
        self.data_generator = get_data_generator()

        # Create and login test user
        self.user_data = self.data_generator.generate_user()
        self.http_client.post("/api/auth/register", json_data={
            "nickname": self.user_data["nickname"],
            "phone": self.user_data["phone"],
            "password": self.user_data["password"],
            "verifyCode": "123456"
        })

        login_response = self.http_client.post("/api/auth/login", json_data={
            "phone": self.user_data["phone"],
            "password": self.user_data["password"]
        })

        if login_response.is_success:
            token = login_response.body.get("data", {}).get("token")
            self.http_client.set_token(token)

    def test_logout_success(self):
        """Test successful logout"""
        response = self.http_client.post("/api/auth/logout")

        assert_success(response)

    def test_logout_without_token(self):
        """Test logout without token"""
        self.http_client.clear_token()
        response = self.http_client.post("/api/auth/logout")

        assert_status_code(response, 401)
