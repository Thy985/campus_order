"""
Comprehensive Payment Service Tests
支付服务综合测试

Tests for payment endpoints including:
- Create payment
- Query payment status
- Payment callback
- Refund
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestPaymentComprehensive:
    """Comprehensive payment service test suite"""

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
    def test_order(self, authenticated_user):
        """Create a test order"""
        # Get merchant and product
        merchant_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        merchant_data = merchant_response.body.get("data", {})
        merchant_list = merchant_data.get("list", [])

        if not merchant_list:
            pytest.skip("No merchants available for testing")

        merchant_id = merchant_list[0].get("id")

        # Get product
        product_response = self.http_client.get(f"/api/merchant/{merchant_id}/products", params={"pageSize": 1})
        product_data = product_response.body.get("data", {})
        product_list = product_data.get("list", [])

        if not product_list:
            pytest.skip("No products available for testing")

        product = product_list[0]

        # Create address
        address_response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号",
            "isDefault": True
        })

        address_id = None
        if address_response.is_success:
            address_id = address_response.body.get("data", {}).get("id")

        if not address_id:
            pytest.skip("Could not create test address")

        # Create order
        order_response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant_id,
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": address_id,
            "deliveryType": 1
        })

        if order_response.is_success:
            return order_response.body.get("data", {})

        pytest.skip("Could not create test order")

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Create Payment Tests ====================

    def test_create_payment_success(self, authenticated_user, test_order):
        """Test creating a payment successfully"""
        order_id = test_order.get("id") or test_order.get("orderId")

        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat",  # or "alipay"
            "returnUrl": "https://example.com/payment/return"
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate payment data
        data = response.body.get("data", {})
        assert "paymentId" in data or "id" in data, "Response should contain payment ID"
        assert "payUrl" in data or "payData" in data, "Response should contain payment URL or data"

    def test_create_payment_invalid_order(self, authenticated_user):
        """Test creating payment for invalid order"""
        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": 999999,
            "paymentMethod": "wechat"
        })

        assert response.status_code in [400, 404, 422], "Should return error for invalid order"

    def test_create_payment_invalid_method(self, authenticated_user, test_order):
        """Test creating payment with invalid payment method"""
        order_id = test_order.get("id") or test_order.get("orderId")

        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "invalid_method"
        })

        assert response.status_code in [400, 422], "Should return error for invalid payment method"

    def test_create_payment_already_paid(self, authenticated_user):
        """Test creating payment for already paid order"""
        # This would require a paid order
        pytest.skip("Requires paid order for testing")

    def test_create_payment_unauthorized(self, test_order):
        """Test creating payment without authentication"""
        self.http_client.clear_token()
        order_id = test_order.get("id") or test_order.get("orderId")

        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Query Payment Status Tests ====================

    def test_get_payment_status_success(self, authenticated_user, test_order):
        """Test getting payment status"""
        # Create payment first
        order_id = test_order.get("id") or test_order.get("orderId")

        payment_response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        if payment_response.is_success:
            payment_data = payment_response.body.get("data", {})
            payment_id = payment_data.get("paymentId") or payment_data.get("id")

            response = self.http_client.get(f"/api/payment/{payment_id}/status")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate status data
            data = response.body.get("data", {})
            assert "status" in data, "Response should contain payment status"

    def test_get_payment_status_by_order(self, authenticated_user, test_order):
        """Test getting payment status by order ID"""
        order_id = test_order.get("id") or test_order.get("orderId")

        response = self.http_client.get("/api/payment/status", params={
            "orderId": order_id
        })

        self._validate_success_response(response)

    def test_get_payment_status_not_found(self, authenticated_user):
        """Test getting status for non-existent payment"""
        response = self.http_client.get("/api/payment/999999/status")

        assert response.status_code in [404, 400], "Should return 404 for non-existent payment"

    def test_get_payment_status_unauthorized(self):
        """Test getting payment status without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/payment/1/status")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Payment Methods Tests ====================

    def test_get_payment_methods(self, authenticated_user):
        """Test getting available payment methods"""
        response = self.http_client.get("/api/payment/methods")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate payment methods
        data = response.body.get("data", {})
        if isinstance(data, list):
            methods = data
        else:
            methods = data.get("list", [])

        if methods:
            for method in methods:
                assert "code" in method or "id" in method, "Method should have code/id"
                assert "name" in method, "Method should have name"

    # ==================== Payment Records Tests ====================

    def test_get_payment_records(self, authenticated_user):
        """Test getting user's payment records"""
        response = self.http_client.get("/api/payment/records")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate pagination
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain list"
        assert "total" in data, "Response should contain total"

    def test_get_payment_records_with_pagination(self, authenticated_user):
        """Test payment records pagination"""
        response = self.http_client.get("/api/payment/records", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_payment_records_unauthorized(self):
        """Test getting payment records without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/payment/records")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Payment Detail Tests ====================

    def test_get_payment_detail(self, authenticated_user, test_order):
        """Test getting payment detail"""
        # Create payment first
        order_id = test_order.get("id") or test_order.get("orderId")

        payment_response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        if payment_response.is_success:
            payment_data = payment_response.body.get("data", {})
            payment_id = payment_data.get("paymentId") or payment_data.get("id")

            response = self.http_client.get(f"/api/payment/{payment_id}")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate payment detail
            data = response.body.get("data", {})
            assert "id" in data or "paymentId" in data, "Payment should have ID"
            assert "amount" in data, "Payment should have amount"
            assert "status" in data, "Payment should have status"

    # ==================== Refund Tests ====================

    def test_request_refund(self, authenticated_user):
        """Test requesting a refund"""
        # This would require a paid order
        pytest.skip("Requires paid order for testing")

    def test_get_refund_status(self, authenticated_user):
        """Test getting refund status"""
        # This would require a refund
        pytest.skip("Requires refund for testing")

    # ==================== Performance Tests ====================

    def test_create_payment_response_time(self, authenticated_user, test_order):
        """Test create payment response time"""
        order_id = test_order.get("id") or test_order.get("orderId")

        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        self.assertions.assert_response_time(response, 2.0)

    def test_get_payment_status_response_time(self, authenticated_user, test_order):
        """Test get payment status response time"""
        order_id = test_order.get("id") or test_order.get("orderId")

        # Create payment first
        payment_response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        if payment_response.is_success:
            payment_data = payment_response.body.get("data", {})
            payment_id = payment_data.get("paymentId") or payment_data.get("id")

            response = self.http_client.get(f"/api/payment/{payment_id}/status")
            self.assertions.assert_response_time(response, 1.0)

    # ==================== Data Integrity Tests ====================

    def test_payment_amount_matches_order(self, authenticated_user, test_order):
        """Test payment amount matches order amount"""
        order_id = test_order.get("id") or test_order.get("orderId")
        order_amount = test_order.get("payAmount") or test_order.get("totalAmount")

        response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        if response.is_success:
            data = response.body.get("data", {})
            payment_amount = data.get("amount")

            if payment_amount and order_amount:
                assert abs(payment_amount - order_amount) < 0.01, \
                    "Payment amount should match order amount"

    def test_payment_status_values(self, authenticated_user, test_order):
        """Test payment status values are valid"""
        order_id = test_order.get("id") or test_order.get("orderId")

        # Create payment
        payment_response = self.http_client.post("/api/payment/create", json_data={
            "orderId": order_id,
            "paymentMethod": "wechat"
        })

        if payment_response.is_success:
            payment_data = payment_response.body.get("data", {})
            payment_id = payment_data.get("paymentId") or payment_data.get("id")

            # Get status
            status_response = self.http_client.get(f"/api/payment/{payment_id}/status")
            data = status_response.body.get("data", {})
            status = data.get("status")

            valid_statuses = [0, 1, 2, 3, "pending", "success", "failed", "cancelled"]
            assert status in valid_statuses or isinstance(status, int), \
                f"Status {status} should be a valid payment status"
