"""
Comprehensive Notification Service Tests
通知服务综合测试

Tests for notification endpoints including:
- Notification list
- Unread count
- Mark as read
- Delete notification
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestNotificationComprehensive:
    """Comprehensive notification service test suite"""

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

    # ==================== Notification List Tests ====================

    def test_get_notification_list_success(self, authenticated_user):
        """Test getting notification list"""
        response = self.http_client.get("/api/notification/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain notification list"
        assert "total" in data, "Response should contain total"

    def test_get_notification_list_with_pagination(self, authenticated_user):
        """Test notification list pagination"""
        response = self.http_client.get("/api/notification/list", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_notification_list_with_type_filter(self, authenticated_user):
        """Test getting notifications by type"""
        response = self.http_client.get("/api/notification/list", params={
            "type": "order"  # or "system", "promotion"
        })

        self._validate_success_response(response)

    def test_get_notification_list_with_read_filter(self, authenticated_user):
        """Test getting notifications by read status"""
        response = self.http_client.get("/api/notification/list", params={
            "isRead": False
        })

        self._validate_success_response(response)

    def test_get_notification_list_unauthorized(self):
        """Test getting notification list without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/notification/list")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Unread Count Tests ====================

    def test_get_unread_count_success(self, authenticated_user):
        """Test getting unread notification count"""
        response = self.http_client.get("/api/notification/unread-count")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate count data
        data = response.body.get("data", {})
        count = data.get("count") if isinstance(data, dict) else data
        assert isinstance(count, int) or count is None, "Count should be integer"
        if isinstance(count, int):
            assert count >= 0, "Count should be non-negative"

    def test_get_unread_count_unauthorized(self):
        """Test getting unread count without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/notification/unread-count")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Mark as Read Tests ====================

    def test_mark_notification_as_read(self, authenticated_user):
        """Test marking a notification as read"""
        # First get a notification ID
        list_response = self.http_client.get("/api/notification/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        notification_list = data.get("list", [])

        if notification_list:
            notification_id = notification_list[0].get("id")
            response = self.http_client.post(f"/api/notification/{notification_id}/read")

            self._validate_success_response(response)

    def test_mark_all_notifications_as_read(self, authenticated_user):
        """Test marking all notifications as read"""
        response = self.http_client.post("/api/notification/read-all")

        self._validate_success_response(response)

    def test_mark_notification_as_read_unauthorized(self):
        """Test marking notification as read without authentication"""
        self.http_client.clear_token()
        response = self.http_client.post("/api/notification/1/read")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Delete Notification Tests ====================

    def test_delete_notification(self, authenticated_user):
        """Test deleting a notification"""
        # First get a notification ID
        list_response = self.http_client.get("/api/notification/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        notification_list = data.get("list", [])

        if notification_list:
            notification_id = notification_list[0].get("id")
            response = self.http_client.delete(f"/api/notification/{notification_id}")

            self._validate_success_response(response)

    def test_delete_notification_unauthorized(self):
        """Test deleting notification without authentication"""
        self.http_client.clear_token()
        response = self.http_client.delete("/api/notification/1")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Notification Detail Tests ====================

    def test_get_notification_detail(self, authenticated_user):
        """Test getting notification detail"""
        # First get a notification ID
        list_response = self.http_client.get("/api/notification/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        notification_list = data.get("list", [])

        if notification_list:
            notification_id = notification_list[0].get("id")
            response = self.http_client.get(f"/api/notification/{notification_id}")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate notification data
            data = response.body.get("data", {})
            assert "id" in data, "Notification should have ID"
            assert "title" in data or "content" in data, "Notification should have title or content"

    # ==================== Performance Tests ====================

    def test_get_notification_list_response_time(self, authenticated_user):
        """Test get notification list response time"""
        response = self.http_client.get("/api/notification/list")
        self.assertions.assert_response_time(response, 1.0)

    def test_get_unread_count_response_time(self, authenticated_user):
        """Test get unread count response time"""
        response = self.http_client.get("/api/notification/unread-count")
        self.assertions.assert_response_time(response, 0.5)

    # ==================== Data Integrity Tests ====================

    def test_notification_data_types(self, authenticated_user):
        """Test notification data types"""
        response = self.http_client.get("/api/notification/list")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        notifications = data.get("list", [])

        if notifications:
            notification = notifications[0]
            if "id" in notification:
                assert isinstance(notification["id"], (int, str)), "ID should be int or string"
            if "isRead" in notification:
                assert isinstance(notification["isRead"], bool), "IsRead should be boolean"
            if "createTime" in notification:
                assert isinstance(notification["createTime"], str), "CreateTime should be string"

    def test_unread_count_non_negative(self, authenticated_user):
        """Test unread count is non-negative"""
        response = self.http_client.get("/api/notification/unread-count")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        count = data.get("count") if isinstance(data, dict) else data

        if isinstance(count, int):
            assert count >= 0, "Unread count should be non-negative"
