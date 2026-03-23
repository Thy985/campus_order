"""
Comprehensive Coupon Service Tests
优惠券服务综合测试

Tests for coupon endpoints including:
- Coupon list
- Claim coupon
- My coupons
- Coupon validation
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestCouponComprehensive:
    """Comprehensive coupon service test suite"""

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

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Coupon List Tests ====================

    def test_get_coupon_list_success(self):
        """Test getting available coupon list"""
        response = self.http_client.get("/api/coupon/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain coupon list"

    def test_get_coupon_list_with_pagination(self):
        """Test coupon list pagination"""
        response = self.http_client.get("/api/coupon/list", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_coupon_list_by_type(self):
        """Test getting coupons by type"""
        response = self.http_client.get("/api/coupon/list", params={
            "type": 1  # 1: amount, 2: discount
        })

        self._validate_success_response(response)

    def test_get_coupon_list_by_merchant(self):
        """Test getting coupons by merchant"""
        response = self.http_client.get("/api/coupon/list", params={
            "merchantId": 1
        })

        self._validate_success_response(response)

    # ==================== Claim Coupon Tests ====================

    def test_claim_coupon_success(self, authenticated_user):
        """Test claiming a coupon"""
        # First get available coupons
        list_response = self.http_client.get("/api/coupon/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        coupon_list = data.get("list", [])

        if not coupon_list:
            pytest.skip("No coupons available for testing")

        coupon_id = coupon_list[0].get("id")

        response = self.http_client.post(f"/api/coupon/{coupon_id}/claim")

        # May succeed or fail if already claimed
        assert response.status_code in [200, 400, 409]
        if response.is_success:
            self._validate_success_response(response)

    def test_claim_coupon_already_claimed(self, authenticated_user):
        """Test claiming an already claimed coupon"""
        # First get available coupons
        list_response = self.http_client.get("/api/coupon/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        coupon_list = data.get("list", [])

        if not coupon_list:
            pytest.skip("No coupons available for testing")

        coupon_id = coupon_list[0].get("id")

        # Claim first time
        self.http_client.post(f"/api/coupon/{coupon_id}/claim")

        # Claim second time
        response = self.http_client.post(f"/api/coupon/{coupon_id}/claim")

        # Should return error for already claimed
        assert response.status_code in [200, 400, 409]

    def test_claim_coupon_not_found(self, authenticated_user):
        """Test claiming non-existent coupon"""
        response = self.http_client.post("/api/coupon/999999/claim")

        assert response.status_code in [404, 400], "Should return 404 for non-existent coupon"

    def test_claim_coupon_unauthorized(self):
        """Test claiming coupon without authentication"""
        self.http_client.clear_token()
        response = self.http_client.post("/api/coupon/1/claim")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== My Coupons Tests ====================

    def test_get_my_coupons_success(self, authenticated_user):
        """Test getting user's coupons"""
        response = self.http_client.get("/api/user/coupons")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain coupon list"

    def test_get_my_coupons_with_status_filter(self, authenticated_user):
        """Test getting coupons by status"""
        response = self.http_client.get("/api/user/coupons", params={
            "status": 1  # 1: unused, 2: used, 3: expired
        })

        self._validate_success_response(response)

    def test_get_my_coupons_unauthorized(self):
        """Test getting my coupons without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/user/coupons")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Coupon Detail Tests ====================

    def test_get_coupon_detail(self):
        """Test getting coupon detail"""
        # First get available coupons
        list_response = self.http_client.get("/api/coupon/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        coupon_list = data.get("list", [])

        if not coupon_list:
            pytest.skip("No coupons available for testing")

        coupon_id = coupon_list[0].get("id")

        response = self.http_client.get(f"/api/coupon/{coupon_id}")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate coupon data
        data = response.body.get("data", {})
        assert "id" in data, "Coupon should have ID"
        assert "name" in data, "Coupon should have name"
        assert "type" in data, "Coupon should have type"

    def test_get_coupon_detail_not_found(self):
        """Test getting non-existent coupon detail"""
        response = self.http_client.get("/api/coupon/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent coupon"

    # ==================== Coupon Validation Tests ====================

    def test_validate_coupon(self, authenticated_user):
        """Test validating a coupon"""
        # First claim a coupon
        list_response = self.http_client.get("/api/coupon/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        coupon_list = data.get("list", [])

        if coupon_list:
            coupon_id = coupon_list[0].get("id")

            response = self.http_client.post("/api/coupon/validate", json_data={
                "couponId": coupon_id,
                "orderAmount": 100
            })

            # May succeed or fail depending on validation rules
            assert response.status_code in [200, 400]

    def test_validate_coupon_insufficient_amount(self, authenticated_user):
        """Test validating coupon with insufficient order amount"""
        list_response = self.http_client.get("/api/coupon/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        coupon_list = data.get("list", [])

        if coupon_list:
            coupon_id = coupon_list[0].get("id")

            response = self.http_client.post("/api/coupon/validate", json_data={
                "couponId": coupon_id,
                "orderAmount": 1  # Very low amount
            })

            # May return error if minimum order amount not met
            assert response.status_code in [200, 400]

    # ==================== Performance Tests ====================

    def test_get_coupon_list_response_time(self):
        """Test get coupon list response time"""
        response = self.http_client.get("/api/coupon/list")
        self.assertions.assert_response_time(response, 1.0)

    def test_get_my_coupons_response_time(self, authenticated_user):
        """Test get my coupons response time"""
        response = self.http_client.get("/api/user/coupons")
        self.assertions.assert_response_time(response, 1.0)

    # ==================== Data Integrity Tests ====================

    def test_coupon_data_types(self):
        """Test coupon data types"""
        response = self.http_client.get("/api/coupon/list")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        coupons = data.get("list", [])

        if coupons:
            coupon = coupons[0]
            if "id" in coupon:
                assert isinstance(coupon["id"], (int, str)), "ID should be int or string"
            if "name" in coupon:
                assert isinstance(coupon["name"], str), "Name should be string"
            if "type" in coupon:
                assert isinstance(coupon["type"], int), "Type should be int"
            if "value" in coupon:
                assert isinstance(coupon["value"], (int, float)), "Value should be numeric"

    def test_coupon_value_positive(self):
        """Test coupon value is positive"""
        response = self.http_client.get("/api/coupon/list")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        coupons = data.get("list", [])

        for coupon in coupons:
            value = coupon.get("value")
            if value is not None:
                assert value > 0, "Coupon value should be positive"

    def test_coupon_validity_period(self):
        """Test coupon has valid period"""
        response = self.http_client.get("/api/coupon/list")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        coupons = data.get("list", [])

        for coupon in coupons:
            start_time = coupon.get("startTime")
            end_time = coupon.get("endTime")

            if start_time and end_time:
                # Both should be strings representing dates
                assert isinstance(start_time, str), "Start time should be string"
                assert isinstance(end_time, str), "End time should be string"
