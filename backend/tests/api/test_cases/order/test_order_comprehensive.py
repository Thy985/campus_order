"""
Comprehensive Order Service Tests
订单服务综合测试

Tests for order management endpoints including:
- Create order
- Order list
- Order detail
- Cancel order
- Order status tracking
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestOrderComprehensive:
    """Comprehensive order service test suite"""

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
        user_id = login_response.body.get("data", {}).get("userId") or \
                  login_response.body.get("data", {}).get("id")

        if token:
            self.http_client.set_token(token)

        return {**user_data, "token": token, "userId": user_id}

    @pytest.fixture
    def test_merchant_and_product(self):
        """Get a test merchant and product"""
        # Get merchant list
        merchant_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        merchant_data = merchant_response.body.get("data", {})
        merchant_list = merchant_data.get("list", [])

        if not merchant_list:
            pytest.skip("No merchants available for testing")

        merchant = merchant_list[0]
        merchant_id = merchant.get("id")

        # Get products for this merchant
        product_response = self.http_client.get(f"/api/merchant/{merchant_id}/products", params={"pageSize": 1})
        product_data = product_response.body.get("data", {})
        product_list = product_data.get("list", [])

        if not product_list:
            pytest.skip("No products available for testing")

        return {
            "merchant": merchant,
            "product": product_list[0]
        }

    @pytest.fixture
    def test_address(self, authenticated_user):
        """Create a test address for the user"""
        response = self.http_client.post("/api/address", json_data={
            "receiverName": self.data_generator.generate_nickname(),
            "receiverPhone": self.data_generator.generate_phone(),
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园南路88号",
            "isDefault": True
        })

        if response.is_success:
            data = response.body.get("data", {})
            return data
        return None

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Create Order Tests ====================

    def test_create_order_success(self, authenticated_user, test_merchant_and_product, test_address):
        """Test creating an order successfully"""
        if not test_address:
            pytest.skip("Could not create test address")

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]
        address_id = test_address.get("id")

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{
                "productId": product.get("id"),
                "quantity": 1
            }],
            "addressId": address_id,
            "remark": "测试订单",
            "deliveryType": 1
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate order data
        data = response.body.get("data", {})
        assert "id" in data or "orderId" in data, "Response should contain order ID"
        assert "orderNo" in data, "Response should contain order number"

    def test_create_order_multiple_items(self, authenticated_user, test_merchant_and_product, test_address):
        """Test creating an order with multiple items"""
        if not test_address:
            pytest.skip("Could not create test address")

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]
        address_id = test_address.get("id")

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [
                {"productId": product.get("id"), "quantity": 2},
                {"productId": product.get("id"), "quantity": 1}
            ],
            "addressId": address_id,
            "deliveryType": 1
        })

        self._validate_success_response(response)

    def test_create_order_invalid_product(self, authenticated_user, test_address):
        """Test creating an order with invalid product"""
        if not test_address:
            pytest.skip("Could not create test address")

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": 1,
            "items": [{"productId": 999999, "quantity": 1}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        assert response.status_code in [400, 404, 422], "Should return error for invalid product"

    def test_create_order_insufficient_stock(self, authenticated_user, test_merchant_and_product, test_address):
        """Test creating an order with quantity exceeding stock"""
        if not test_address:
            pytest.skip("Could not create test address")

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 9999}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        # May return error or allow with backorder
        assert response.status_code in [200, 400, 422, 409]

    def test_create_order_unauthorized(self, test_merchant_and_product):
        """Test creating an order without authentication"""
        self.http_client.clear_token()

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": 1,
            "deliveryType": 1
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    def test_create_order_missing_required_fields(self, authenticated_user):
        """Test creating an order with missing required fields"""
        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": 1
            # Missing items, addressId
        })

        assert response.status_code == 400, "Should return 400 for missing fields"

    # ==================== Order List Tests ====================

    def test_get_order_list_success(self, authenticated_user):
        """Test getting order list"""
        response = self.http_client.get("/api/order/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate pagination structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain list"
        assert "total" in data, "Response should contain total"

    def test_get_order_list_with_pagination(self, authenticated_user):
        """Test order list pagination"""
        response = self.http_client.get("/api/order/list", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_order_list_with_status_filter(self, authenticated_user):
        """Test getting orders by status"""
        response = self.http_client.get("/api/order/list", params={
            "status": 1  # Pending payment
        })

        self._validate_success_response(response)

    def test_get_order_list_unauthorized(self):
        """Test getting order list without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/order/list")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Order Detail Tests ====================

    def test_get_order_detail_success(self, authenticated_user, test_merchant_and_product, test_address):
        """Test getting order detail"""
        if not test_address:
            pytest.skip("Could not create test address")

        # Create an order first
        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        create_response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        if create_response.is_success:
            order_data = create_response.body.get("data", {})
            order_id = order_data.get("id") or order_data.get("orderId")

            response = self.http_client.get(f"/api/order/{order_id}")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate order detail structure
            data = response.body.get("data", {})
            assert "id" in data or "orderId" in data, "Order should have ID"
            assert "orderNo" in data, "Order should have order number"
            assert "status" in data, "Order should have status"

    def test_get_order_detail_not_found(self, authenticated_user):
        """Test getting non-existent order"""
        response = self.http_client.get("/api/order/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent order"

    def test_get_order_detail_unauthorized(self):
        """Test getting order detail without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/order/1")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Cancel Order Tests ====================

    def test_cancel_order_success(self, authenticated_user, test_merchant_and_product, test_address):
        """Test cancelling an order"""
        if not test_address:
            pytest.skip("Could not create test address")

        # Create an order first
        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        create_response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        if create_response.is_success:
            order_data = create_response.body.get("data", {})
            order_id = order_data.get("id") or order_data.get("orderId")

            response = self.http_client.post(f"/api/order/{order_id}/cancel", json_data={
                "reason": "测试取消"
            })

            self._validate_success_response(response)

    def test_cancel_order_already_paid(self, authenticated_user):
        """Test cancelling an already paid order"""
        # This would require a paid order, skip if not available
        pytest.skip("Requires paid order for testing")

    def test_cancel_order_unauthorized(self):
        """Test cancelling order without authentication"""
        self.http_client.clear_token()
        response = self.http_client.post("/api/order/1/cancel", json_data={
            "reason": "测试取消"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Order Status Tests ====================

    def test_get_order_status(self, authenticated_user, test_merchant_and_product, test_address):
        """Test getting order status"""
        if not test_address:
            pytest.skip("Could not create test address")

        # Create an order first
        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        create_response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        if create_response.is_success:
            order_data = create_response.body.get("data", {})
            order_id = order_data.get("id") or order_data.get("orderId")

            response = self.http_client.get(f"/api/order/{order_id}/status")

            self._validate_success_response(response)

    # ==================== Performance Tests ====================

    def test_create_order_response_time(self, authenticated_user, test_merchant_and_product, test_address):
        """Test create order response time"""
        if not test_address:
            pytest.skip("Could not create test address")

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": 1}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        self.assertions.assert_response_time(response, 3.0)

    def test_get_order_list_response_time(self, authenticated_user):
        """Test get order list response time"""
        response = self.http_client.get("/api/order/list")
        self.assertions.assert_response_time(response, 1.5)

    # ==================== Data Integrity Tests ====================

    def test_order_amount_calculation(self, authenticated_user, test_merchant_and_product, test_address):
        """Test order amount calculation"""
        if not test_address:
            pytest.skip("Could not create test address")

        merchant = test_merchant_and_product["merchant"]
        product = test_merchant_and_product["product"]
        quantity = 2

        response = self.http_client.post("/api/order/create", json_data={
            "merchantId": merchant.get("id"),
            "items": [{"productId": product.get("id"), "quantity": quantity}],
            "addressId": test_address.get("id"),
            "deliveryType": 1
        })

        if response.is_success:
            data = response.body.get("data", {})
            # Verify total amount calculation
            if "totalAmount" in data and "items" in data:
                expected_total = product.get("price", 0) * quantity
                actual_total = data.get("totalAmount", 0)
                assert abs(actual_total - expected_total) < 0.01, "Order total should match calculation"
