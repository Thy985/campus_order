"""
Comprehensive Merchant Admin Tests
商家端综合测试

Tests for merchant admin endpoints including:
- Merchant order management
- Accept order
- Product management (CRUD)
- Merchant statistics
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestMerchantAdminComprehensive:
    """Comprehensive merchant admin test suite"""

    @pytest.fixture(autouse=True)
    def setup(self, fresh_http_client: HTTPClient, data_generator: DataGenerator):
        """Setup test fixtures"""
        self.http_client = fresh_http_client
        self.data_generator = data_generator
        self.assertions = APIAssertions()
        self.validator = ResponseValidator()
        self.field_validator = FieldValidator()

    @pytest.fixture
    def authenticated_merchant(self):
        """Create and authenticate as a merchant"""
        # Note: This assumes merchant login is similar to user login
        # In real implementation, merchant auth might be different
        user_data = self.data_generator.generate_user()

        # Try merchant login
        login_response = self.http_client.post("/api/merchant/auth/login", json_data={
            "username": "test_merchant",
            "password": "test_password"
        })

        if not login_response.is_success:
            pytest.skip("Merchant authentication not available")

        token = login_response.body.get("data", {}).get("token") or \
                login_response.body.get("data", {}).get("accessToken")
        merchant_id = login_response.body.get("data", {}).get("merchantId") or \
                      login_response.body.get("data", {}).get("id")

        if token:
            self.http_client.set_token(token)

        return {**user_data, "token": token, "merchantId": merchant_id}

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Merchant Order Management Tests ====================

    def test_get_merchant_orders_success(self, authenticated_merchant):
        """Test getting merchant's orders"""
        response = self.http_client.get("/api/merchant/orders")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain order list"
        assert "total" in data, "Response should contain total"

    def test_get_merchant_orders_with_status_filter(self, authenticated_merchant):
        """Test getting merchant orders by status"""
        response = self.http_client.get("/api/merchant/orders", params={
            "status": 1  # Pending
        })

        self._validate_success_response(response)

    def test_get_merchant_orders_with_pagination(self, authenticated_merchant):
        """Test merchant orders pagination"""
        response = self.http_client.get("/api/merchant/orders", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_merchant_orders_unauthorized(self):
        """Test getting merchant orders without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/merchant/orders")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Accept Order Tests ====================

    def test_accept_order_success(self, authenticated_merchant):
        """Test accepting an order"""
        # First get pending orders
        orders_response = self.http_client.get("/api/merchant/orders", params={
            "status": 1,  # Pending
            "pageSize": 1
        })

        data = orders_response.body.get("data", {})
        orders = data.get("list", [])

        if not orders:
            pytest.skip("No pending orders available for testing")

        order_id = orders[0].get("id")

        response = self.http_client.post(f"/api/merchant/orders/{order_id}/accept")

        self._validate_success_response(response)

    def test_reject_order_success(self, authenticated_merchant):
        """Test rejecting an order"""
        # First get pending orders
        orders_response = self.http_client.get("/api/merchant/orders", params={
            "status": 1,
            "pageSize": 1
        })

        data = orders_response.body.get("data", {})
        orders = data.get("list", [])

        if not orders:
            pytest.skip("No pending orders available for testing")

        order_id = orders[0].get("id")

        response = self.http_client.post(f"/api/merchant/orders/{order_id}/reject", json_data={
            "reason": "商家暂时无法接单"
        })

        self._validate_success_response(response)

    def test_update_order_status(self, authenticated_merchant):
        """Test updating order status"""
        orders_response = self.http_client.get("/api/merchant/orders", params={"pageSize": 1})
        data = orders_response.body.get("data", {})
        orders = data.get("list", [])

        if not orders:
            pytest.skip("No orders available for testing")

        order_id = orders[0].get("id")

        response = self.http_client.put(f"/api/merchant/orders/{order_id}/status", json_data={
            "status": 3  # Preparing
        })

        self._validate_success_response(response)

    # ==================== Product Management Tests ====================

    def test_get_merchant_products(self, authenticated_merchant):
        """Test getting merchant's products"""
        response = self.http_client.get("/api/merchant/products")

        self._validate_success_response(response, ["code", "message", "data"])

        data = response.body.get("data", {})
        assert "list" in data, "Response should contain product list"

    def test_create_product_success(self, authenticated_merchant):
        """Test creating a new product"""
        response = self.http_client.post("/api/merchant/products", json_data={
            "name": f"测试商品_{self.data_generator.generate_username()}",
            "description": "这是一个测试商品",
            "price": 29.99,
            "originalPrice": 39.99,
            "stock": 100,
            "categoryId": 1,
            "unit": "份",
            "image": "https://example.com/image.jpg",
            "status": 1
        })

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate created product
        data = response.body.get("data", {})
        assert "id" in data, "Response should contain product ID"

    def test_create_product_missing_required_fields(self, authenticated_merchant):
        """Test creating product with missing fields"""
        response = self.http_client.post("/api/merchant/products", json_data={
            "name": "测试商品"
            # Missing other required fields
        })

        assert response.status_code == 400, "Should return 400 for missing fields"

    def test_update_product_success(self, authenticated_merchant):
        """Test updating a product"""
        # First get products
        products_response = self.http_client.get("/api/merchant/products", params={"pageSize": 1})
        data = products_response.body.get("data", {})
        products = data.get("list", [])

        if not products:
            pytest.skip("No products available for testing")

        product_id = products[0].get("id")

        response = self.http_client.put(f"/api/merchant/products/{product_id}", json_data={
            "name": "更新后的商品名称",
            "price": 35.99,
            "stock": 150
        })

        self._validate_success_response(response)

    def test_delete_product_success(self, authenticated_merchant):
        """Test deleting a product"""
        # Create a product first
        create_response = self.http_client.post("/api/merchant/products", json_data={
            "name": f"待删除商品_{self.data_generator.generate_username()}",
            "price": 19.99,
            "stock": 50,
            "categoryId": 1,
            "unit": "份"
        })

        if create_response.is_success:
            product_id = create_response.body.get("data", {}).get("id")

            response = self.http_client.delete(f"/api/merchant/products/{product_id}")

            self._validate_success_response(response)

    def test_update_product_stock(self, authenticated_merchant):
        """Test updating product stock"""
        products_response = self.http_client.get("/api/merchant/products", params={"pageSize": 1})
        data = products_response.body.get("data", {})
        products = data.get("list", [])

        if not products:
            pytest.skip("No products available for testing")

        product_id = products[0].get("id")

        response = self.http_client.put(f"/api/merchant/products/{product_id}/stock", json_data={
            "stock": 200
        })

        self._validate_success_response(response)

    def test_toggle_product_status(self, authenticated_merchant):
        """Test toggling product status (on/off shelf)"""
        products_response = self.http_client.get("/api/merchant/products", params={"pageSize": 1})
        data = products_response.body.get("data", {})
        products = data.get("list", [])

        if not products:
            pytest.skip("No products available for testing")

        product_id = products[0].get("id")

        response = self.http_client.post(f"/api/merchant/products/{product_id}/toggle")

        self._validate_success_response(response)

    # ==================== Merchant Statistics Tests ====================

    def test_get_merchant_statistics(self, authenticated_merchant):
        """Test getting merchant statistics"""
        response = self.http_client.get("/api/merchant/statistics")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_get_merchant_daily_statistics(self, authenticated_merchant):
        """Test getting daily statistics"""
        response = self.http_client.get("/api/merchant/statistics/daily", params={
            "date": "2024-01-01"
        })

        self._validate_success_response(response)

    def test_get_merchant_order_statistics(self, authenticated_merchant):
        """Test getting order statistics"""
        response = self.http_client.get("/api/merchant/statistics/orders")

        self._validate_success_response(response)

    # ==================== Merchant Profile Tests ====================

    def test_get_merchant_profile(self, authenticated_merchant):
        """Test getting merchant profile"""
        response = self.http_client.get("/api/merchant/profile")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_update_merchant_profile(self, authenticated_merchant):
        """Test updating merchant profile"""
        response = self.http_client.put("/api/merchant/profile", json_data={
            "phone": self.data_generator.generate_phone(),
            "businessHours": "09:00-22:00",
            "description": "更新后的商家描述"
        })

        self._validate_success_response(response)

    # ==================== Performance Tests ====================

    def test_get_merchant_orders_response_time(self, authenticated_merchant):
        """Test get merchant orders response time"""
        response = self.http_client.get("/api/merchant/orders")
        self.assertions.assert_response_time(response, 1.5)

    def test_get_merchant_products_response_time(self, authenticated_merchant):
        """Test get merchant products response time"""
        response = self.http_client.get("/api/merchant/products")
        self.assertions.assert_response_time(response, 1.0)

    # ==================== Data Integrity Tests ====================

    def test_merchant_order_data_types(self, authenticated_merchant):
        """Test merchant order data types"""
        response = self.http_client.get("/api/merchant/orders", params={"pageSize": 1})

        self._validate_success_response(response)

        data = response.body.get("data", {})
        orders = data.get("list", [])

        if orders:
            order = orders[0]
            if "id" in order:
                assert isinstance(order["id"], (int, str)), "ID should be int or string"
            if "status" in order:
                assert isinstance(order["status"], int), "Status should be int"
            if "totalAmount" in order:
                assert isinstance(order["totalAmount"], (int, float)), "Total amount should be numeric"

    def test_product_price_validation(self, authenticated_merchant):
        """Test product price validation"""
        response = self.http_client.post("/api/merchant/products", json_data={
            "name": "测试商品",
            "price": -10,  # Invalid negative price
            "stock": 100,
            "categoryId": 1
        })

        assert response.status_code in [400, 422], "Should return error for negative price"
