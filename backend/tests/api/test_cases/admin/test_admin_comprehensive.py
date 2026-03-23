"""
Comprehensive Admin Tests
管理员端综合测试

Tests for admin endpoints including:
- User management
- Order management
- Merchant management
- System statistics
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestAdminComprehensive:
    """Comprehensive admin test suite"""

    @pytest.fixture(autouse=True)
    def setup(self, fresh_http_client: HTTPClient, data_generator: DataGenerator):
        """Setup test fixtures"""
        self.http_client = fresh_http_client
        self.data_generator = data_generator
        self.assertions = APIAssertions()
        self.validator = ResponseValidator()
        self.field_validator = FieldValidator()

    @pytest.fixture
    def authenticated_admin(self):
        """Create and authenticate as an admin"""
        # Try admin login
        login_response = self.http_client.post("/api/admin/auth/login", json_data={
            "username": "admin",
            "password": "admin_password"
        })

        if not login_response.is_success:
            pytest.skip("Admin authentication not available")

        token = login_response.body.get("data", {}).get("token") or \
                login_response.body.get("data", {}).get("accessToken")

        if token:
            self.http_client.set_token(token)

        return {"token": token, "username": "admin"}

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== User Management Tests ====================

    def test_get_user_list_success(self, authenticated_admin):
        """Test getting user list"""
        response = self.http_client.get("/api/admin/users")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain user list"
        assert "total" in data, "Response should contain total"

    def test_get_user_list_with_pagination(self, authenticated_admin):
        """Test user list pagination"""
        response = self.http_client.get("/api/admin/users", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_user_list_with_search(self, authenticated_admin):
        """Test searching users"""
        response = self.http_client.get("/api/admin/users", params={
            "keyword": "test"
        })

        self._validate_success_response(response)

    def test_get_user_detail(self, authenticated_admin):
        """Test getting user detail"""
        # First get user list
        list_response = self.http_client.get("/api/admin/users", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        users = data.get("list", [])

        if not users:
            pytest.skip("No users available for testing")

        user_id = users[0].get("id")

        response = self.http_client.get(f"/api/admin/users/{user_id}")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_update_user_status(self, authenticated_admin):
        """Test updating user status (enable/disable)"""
        list_response = self.http_client.get("/api/admin/users", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        users = data.get("list", [])

        if not users:
            pytest.skip("No users available for testing")

        user_id = users[0].get("id")

        response = self.http_client.put(f"/api/admin/users/{user_id}/status", json_data={
            "status": 0  # Disable
        })

        self._validate_success_response(response)

    def test_delete_user(self, authenticated_admin):
        """Test deleting a user"""
        # This is a sensitive operation, may not be available
        pytest.skip("User deletion may not be available")

    # ==================== Order Management Tests ====================

    def test_get_all_orders_success(self, authenticated_admin):
        """Test getting all orders"""
        response = self.http_client.get("/api/admin/orders")

        self._validate_success_response(response, ["code", "message", "data"])

        data = response.body.get("data", {})
        assert "list" in data, "Response should contain order list"

    def test_get_all_orders_with_filters(self, authenticated_admin):
        """Test getting orders with filters"""
        response = self.http_client.get("/api/admin/orders", params={
            "status": 1,
            "startDate": "2024-01-01",
            "endDate": "2024-12-31"
        })

        self._validate_success_response(response)

    def test_get_order_detail_admin(self, authenticated_admin):
        """Test getting order detail as admin"""
        # First get order list
        list_response = self.http_client.get("/api/admin/orders", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        orders = data.get("list", [])

        if not orders:
            pytest.skip("No orders available for testing")

        order_id = orders[0].get("id")

        response = self.http_client.get(f"/api/admin/orders/{order_id}")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_cancel_order_admin(self, authenticated_admin):
        """Test cancelling order as admin"""
        list_response = self.http_client.get("/api/admin/orders", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        orders = data.get("list", [])

        if not orders:
            pytest.skip("No orders available for testing")

        order_id = orders[0].get("id")

        response = self.http_client.post(f"/api/admin/orders/{order_id}/cancel", json_data={
            "reason": "管理员取消"
        })

        self._validate_success_response(response)

    def test_refund_order_admin(self, authenticated_admin):
        """Test refunding order as admin"""
        # This would require a paid order
        pytest.skip("Requires paid order for testing")

    # ==================== Merchant Management Tests ====================

    def test_get_merchant_list_admin(self, authenticated_admin):
        """Test getting merchant list as admin"""
        response = self.http_client.get("/api/admin/merchants")

        self._validate_success_response(response, ["code", "message", "data"])

        data = response.body.get("data", {})
        assert "list" in data, "Response should contain merchant list"

    def test_create_merchant_admin(self, authenticated_admin):
        """Test creating a new merchant as admin"""
        response = self.http_client.post("/api/admin/merchants", json_data={
            "name": f"测试商家_{self.data_generator.generate_username()}",
            "phone": self.data_generator.generate_phone(),
            "address": "测试地址",
            "categoryId": 1,
            "description": "这是一个测试商家",
            "businessHours": "09:00-22:00",
            "minOrderAmount": 20,
            "deliveryFee": 5
        })

        self._validate_success_response(response, ["code", "message", "data"])

    def test_update_merchant_admin(self, authenticated_admin):
        """Test updating merchant as admin"""
        list_response = self.http_client.get("/api/admin/merchants", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchants = data.get("list", [])

        if not merchants:
            pytest.skip("No merchants available for testing")

        merchant_id = merchants[0].get("id")

        response = self.http_client.put(f"/api/admin/merchants/{merchant_id}", json_data={
            "name": "更新后的商家名称",
            "phone": self.data_generator.generate_phone()
        })

        self._validate_success_response(response)

    def test_update_merchant_status_admin(self, authenticated_admin):
        """Test updating merchant status as admin"""
        list_response = self.http_client.get("/api/admin/merchants", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchants = data.get("list", [])

        if not merchants:
            pytest.skip("No merchants available for testing")

        merchant_id = merchants[0].get("id")

        response = self.http_client.put(f"/api/admin/merchants/{merchant_id}/status", json_data={
            "status": 0  # Disable
        })

        self._validate_success_response(response)

    def test_delete_merchant_admin(self, authenticated_admin):
        """Test deleting merchant as admin"""
        # This is a sensitive operation
        pytest.skip("Merchant deletion may not be available")

    # ==================== System Statistics Tests ====================

    def test_get_system_statistics(self, authenticated_admin):
        """Test getting system statistics"""
        response = self.http_client.get("/api/admin/statistics")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_get_dashboard_data(self, authenticated_admin):
        """Test getting dashboard data"""
        response = self.http_client.get("/api/admin/dashboard")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_get_user_statistics(self, authenticated_admin):
        """Test getting user statistics"""
        response = self.http_client.get("/api/admin/statistics/users")

        self._validate_success_response(response)

    def test_get_order_statistics(self, authenticated_admin):
        """Test getting order statistics"""
        response = self.http_client.get("/api/admin/statistics/orders")

        self._validate_success_response(response)

    def test_get_revenue_statistics(self, authenticated_admin):
        """Test getting revenue statistics"""
        response = self.http_client.get("/api/admin/statistics/revenue")

        self._validate_success_response(response)

    # ==================== System Management Tests ====================

    def test_get_system_settings(self, authenticated_admin):
        """Test getting system settings"""
        response = self.http_client.get("/api/admin/settings")

        self._validate_success_response(response)

    def test_update_system_settings(self, authenticated_admin):
        """Test updating system settings"""
        response = self.http_client.put("/api/admin/settings", json_data={
            "siteName": "校园点餐平台",
            "contactPhone": "400-123-4567"
        })

        self._validate_success_response(response)

    def test_get_system_logs(self, authenticated_admin):
        """Test getting system logs"""
        response = self.http_client.get("/api/admin/logs")

        self._validate_success_response(response)

    # ==================== Performance Tests ====================

    def test_get_user_list_response_time(self, authenticated_admin):
        """Test get user list response time"""
        response = self.http_client.get("/api/admin/users")
        self.assertions.assert_response_time(response, 2.0)

    def test_get_order_list_response_time(self, authenticated_admin):
        """Test get order list response time"""
        response = self.http_client.get("/api/admin/orders")
        self.assertions.assert_response_time(response, 2.0)

    def test_get_statistics_response_time(self, authenticated_admin):
        """Test get statistics response time"""
        response = self.http_client.get("/api/admin/statistics")
        self.assertions.assert_response_time(response, 1.5)

    # ==================== Authorization Tests ====================

    def test_admin_endpoints_unauthorized(self):
        """Test admin endpoints without authentication"""
        self.http_client.clear_token()

        endpoints = [
            ("/api/admin/users", "GET"),
            ("/api/admin/orders", "GET"),
            ("/api/admin/merchants", "GET"),
            ("/api/admin/statistics", "GET"),
        ]

        for endpoint, method in endpoints:
            if method == "GET":
                response = self.http_client.get(endpoint)
            else:
                response = self.http_client.post(endpoint)

            assert response.status_code == 401, \
                f"{endpoint} should return 401 for unauthorized access"

    def test_admin_endpoints_forbidden_for_user(self, authenticated_user):
        """Test admin endpoints with regular user token"""
        # This would require a regular user fixture
        pytest.skip("Requires regular user authentication")

    # ==================== Data Integrity Tests ====================

    def test_user_data_types(self, authenticated_admin):
        """Test user data types"""
        response = self.http_client.get("/api/admin/users", params={"pageSize": 1})

        self._validate_success_response(response)

        data = response.body.get("data", {})
        users = data.get("list", [])

        if users:
            user = users[0]
            if "id" in user:
                assert isinstance(user["id"], (int, str)), "ID should be int or string"
            if "username" in user:
                assert isinstance(user["username"], str), "Username should be string"
            if "status" in user:
                assert isinstance(user["status"], int), "Status should be int"

    def test_order_data_types(self, authenticated_admin):
        """Test order data types"""
        response = self.http_client.get("/api/admin/orders", params={"pageSize": 1})

        self._validate_success_response(response)

        data = response.body.get("data", {})
        orders = data.get("list", [])

        if orders:
            order = orders[0]
            if "id" in order:
                assert isinstance(order["id"], (int, str)), "ID should be int or string"
            if "totalAmount" in order:
                assert isinstance(order["totalAmount"], (int, float)), "Total amount should be numeric"
            if "status" in order:
                assert isinstance(order["status"], int), "Status should be int"
