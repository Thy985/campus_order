"""
Comprehensive Address Service Tests
地址服务综合测试

Tests for address management endpoints including:
- Address list
- Add address
- Update address
- Delete address
- Set default address
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestAddressComprehensive:
    """Comprehensive address service test suite"""

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

        return {**user_data, "token": token}

    @pytest.fixture
    def test_address(self, authenticated_user):
        """Create a test address"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号",
            "isDefault": False
        })

        if response.is_success:
            return response.body.get("data", {})
        return None

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Address List Tests ====================

    def test_get_address_list_success(self, authenticated_user):
        """Test getting address list"""
        response = self.http_client.get("/api/address/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        if isinstance(data, list):
            addresses = data
        else:
            addresses = data.get("list", [])

        assert isinstance(addresses, list), "Response should contain address list"

    def test_get_address_list_unauthorized(self):
        """Test getting address list without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/address/list")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Add Address Tests ====================

    def test_add_address_success(self, authenticated_user):
        """Test adding a new address"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号",
            "isDefault": True
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate created address
        data = response.body.get("data", {})
        assert "id" in data, "Response should contain address ID"

    def test_add_address_missing_required_fields(self, authenticated_user):
        """Test adding address with missing required fields"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": "Test User"
            # Missing other required fields
        })

        assert response.status_code == 400, "Should return 400 for missing fields"

    def test_add_address_invalid_phone(self, authenticated_user):
        """Test adding address with invalid phone"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": "12345678901",  # Invalid phone
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号"
        })

        assert response.status_code in [400, 422], "Should return 400 or 422 for invalid phone"

    def test_add_address_unauthorized(self):
        """Test adding address without authentication"""
        self.http_client.clear_token()
        response = self.http_client.post("/api/address", json_data={
            "receiverName": "Test User",
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Update Address Tests ====================

    def test_update_address_success(self, authenticated_user, test_address):
        """Test updating an address"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")
        new_name = self.data_generator.generate_nickname()

        response = self.http_client.put(f"/api/address/{address_id}", json_data={
            "receiverName": new_name,
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "广州市",
            "district": "天河区",
            "detailAddress": "天河路100号"
        })

        self._validate_success_response(response)

    def test_update_address_partial(self, authenticated_user, test_address):
        """Test partial update of an address"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")

        response = self.http_client.put(f"/api/address/{address_id}", json_data={
            "receiverName": "Updated Name"
        })

        self._validate_success_response(response)

    def test_update_address_not_found(self, authenticated_user):
        """Test updating non-existent address"""
        response = self.http_client.put("/api/address/999999", json_data={
            "receiverName": "Test User"
        })

        assert response.status_code in [404, 400], "Should return 404 for non-existent address"

    def test_update_address_unauthorized(self, test_address):
        """Test updating address without authentication"""
        if not test_address:
            pytest.skip("Could not create test address")

        self.http_client.clear_token()
        address_id = test_address.get("id")

        response = self.http_client.put(f"/api/address/{address_id}", json_data={
            "receiverName": "Test User"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Delete Address Tests ====================

    def test_delete_address_success(self, authenticated_user):
        """Test deleting an address"""
        # Create an address first
        create_response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号"
        })

        if create_response.is_success:
            address_id = create_response.body.get("data", {}).get("id")

            response = self.http_client.delete(f"/api/address/{address_id}")

            self._validate_success_response(response)

    def test_delete_address_not_found(self, authenticated_user):
        """Test deleting non-existent address"""
        response = self.http_client.delete("/api/address/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent address"

    def test_delete_address_unauthorized(self, test_address):
        """Test deleting address without authentication"""
        if not test_address:
            pytest.skip("Could not create test address")

        self.http_client.clear_token()
        address_id = test_address.get("id")

        response = self.http_client.delete(f"/api/address/{address_id}")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Default Address Tests ====================

    def test_set_default_address(self, authenticated_user, test_address):
        """Test setting default address"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")

        response = self.http_client.post(f"/api/address/{address_id}/default")

        self._validate_success_response(response)

    def test_get_default_address(self, authenticated_user):
        """Test getting default address"""
        response = self.http_client.get("/api/address/default")

        # May return address or 404 if no default
        assert response.status_code in [200, 404]
        if response.is_success:
            self._validate_success_response(response)

    # ==================== Address Detail Tests ====================

    def test_get_address_detail(self, authenticated_user, test_address):
        """Test getting address detail"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")

        response = self.http_client.get(f"/api/address/{address_id}")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate address data
        data = response.body.get("data", {})
        assert "id" in data, "Address should have ID"
        assert "receiverName" in data, "Address should have receiver name"
        assert "receiverPhone" in data, "Address should have receiver phone"

    def test_get_address_detail_not_found(self, authenticated_user):
        """Test getting non-existent address detail"""
        response = self.http_client.get("/api/address/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent address"

    # ==================== Performance Tests ====================

    def test_get_address_list_response_time(self, authenticated_user):
        """Test get address list response time"""
        response = self.http_client.get("/api/address/list")
        self.assertions.assert_response_time(response, 1.0)

    def test_add_address_response_time(self, authenticated_user):
        """Test add address response time"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号"
        })
        self.assertions.assert_response_time(response, 1.5)

    # ==================== Data Integrity Tests ====================

    def test_address_data_types(self, authenticated_user, test_address):
        """Test address data types"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")
        response = self.http_client.get(f"/api/address/{address_id}")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        if "id" in data:
            assert isinstance(data["id"], (int, str)), "ID should be int or string"
        if "receiverName" in data:
            assert isinstance(data["receiverName"], str), "Receiver name should be string"
        if "receiverPhone" in data:
            assert isinstance(data["receiverPhone"], str), "Receiver phone should be string"
        if "isDefault" in data:
            assert isinstance(data["isDefault"], bool), "IsDefault should be boolean"

    def test_address_phone_format(self, authenticated_user, test_address):
        """Test address phone format"""
        if not test_address:
            pytest.skip("Could not create test address")

        address_id = test_address.get("id")
        response = self.http_client.get(f"/api/address/{address_id}")

        data = response.body.get("data", {})
        phone = data.get("receiverPhone")
        if phone:
            assert self.field_validator.is_valid_phone(phone), "Phone should be valid Chinese mobile number"
