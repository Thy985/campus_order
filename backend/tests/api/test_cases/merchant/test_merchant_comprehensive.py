"""
Comprehensive Merchant Service Tests
商家服务综合测试

Tests for merchant endpoints including:
- Merchant list
- Merchant detail
- Merchant products
- Merchant categories
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestMerchantComprehensive:
    """Comprehensive merchant service test suite"""

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

    # ==================== Merchant List Tests ====================

    def test_get_merchant_list_success(self):
        """Test getting merchant list without authentication"""
        response = self.http_client.get("/api/merchant/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate pagination structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain list"
        assert "total" in data, "Response should contain total"

    def test_get_merchant_list_with_pagination(self):
        """Test merchant list pagination"""
        response = self.http_client.get("/api/merchant/list", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response, ["code", "message", "data"])

        data = response.body.get("data", {})
        assert "page" in data or "current" in data, "Response should contain page number"
        assert "pageSize" in data or "size" in data, "Response should contain page size"

    def test_get_merchant_list_with_category(self):
        """Test getting merchants by category"""
        response = self.http_client.get("/api/merchant/list", params={
            "categoryId": 1
        })

        self._validate_success_response(response)

    def test_get_merchant_list_with_search(self):
        """Test searching merchants"""
        response = self.http_client.get("/api/merchant/list", params={
            "keyword": "餐厅"
        })

        self._validate_success_response(response)

    def test_get_merchant_list_with_sorting(self):
        """Test merchant list with sorting"""
        response = self.http_client.get("/api/merchant/list", params={
            "sortBy": "rating",
            "sortOrder": "desc"
        })

        self._validate_success_response(response)

    def test_get_merchant_list_invalid_page(self):
        """Test merchant list with invalid page number"""
        response = self.http_client.get("/api/merchant/list", params={
            "page": -1
        })

        # Should either handle gracefully or return error
        assert response.status_code in [200, 400]

    # ==================== Merchant Detail Tests ====================

    def test_get_merchant_detail_success(self):
        """Test getting merchant detail"""
        # First get a merchant ID from list
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate merchant data structure
            merchant_data = response.body.get("data", {})
            assert "id" in merchant_data, "Merchant should have id"
            assert "name" in merchant_data, "Merchant should have name"
            assert "categoryId" in merchant_data, "Merchant should have categoryId"

    def test_get_merchant_detail_not_found(self):
        """Test getting non-existent merchant"""
        response = self.http_client.get("/api/merchant/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent merchant"

    def test_get_merchant_detail_invalid_id(self):
        """Test getting merchant with invalid ID"""
        response = self.http_client.get("/api/merchant/invalid")

        assert response.status_code in [400, 404], "Should return 400 or 404 for invalid ID"

    # ==================== Merchant Products Tests ====================

    def test_get_merchant_products_success(self):
        """Test getting products of a merchant"""
        # First get a merchant ID
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}/products")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate product list structure
            data = response.body.get("data", {})
            assert "list" in data, "Response should contain product list"

    def test_get_merchant_products_with_pagination(self):
        """Test merchant products with pagination"""
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}/products", params={
                "page": 1,
                "pageSize": 10
            })

            self._validate_success_response(response)

    def test_get_merchant_products_not_found(self):
        """Test getting products of non-existent merchant"""
        response = self.http_client.get("/api/merchant/999999/products")

        assert response.status_code in [404, 400], "Should return 404 or 400"

    # ==================== Merchant Categories Tests ====================

    def test_get_merchant_categories_success(self):
        """Test getting merchant categories"""
        response = self.http_client.get("/api/merchant/categories")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate category list
        data = response.body.get("data", {})
        if isinstance(data, list):
            categories = data
        else:
            categories = data.get("list", [])

        if categories:
            for category in categories:
                assert "id" in category, "Category should have id"
                assert "name" in category, "Category should have name"

    # ==================== Merchant Reviews Tests ====================

    def test_get_merchant_reviews_success(self):
        """Test getting merchant reviews"""
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}/reviews")

            self._validate_success_response(response, ["code", "message", "data"])

    def test_get_merchant_reviews_with_rating_filter(self):
        """Test getting merchant reviews with rating filter"""
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}/reviews", params={
                "rating": 5
            })

            self._validate_success_response(response)

    # ==================== Favorite Merchant Tests ====================

    def test_add_merchant_to_favorites(self, authenticated_user):
        """Test adding merchant to favorites"""
        # First get a merchant ID
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.post(f"/api/merchant/{merchant_id}/favorite")

            # May require authentication
            assert response.status_code in [200, 401]
            if response.is_success:
                self._validate_success_response(response)

    def test_get_favorite_merchants(self, authenticated_user):
        """Test getting favorite merchants list"""
        response = self.http_client.get("/api/user/favorites/merchants")

        assert response.status_code in [200, 401]
        if response.is_success:
            self._validate_success_response(response, ["code", "message", "data"])

    # ==================== Performance Tests ====================

    def test_merchant_list_response_time(self):
        """Test merchant list response time"""
        response = self.http_client.get("/api/merchant/list")
        self.assertions.assert_response_time(response, 1.5)

    def test_merchant_detail_response_time(self):
        """Test merchant detail response time"""
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant_id = merchant_list[0].get("id")
            response = self.http_client.get(f"/api/merchant/{merchant_id}")
            self.assertions.assert_response_time(response, 1.0)

    # ==================== Data Integrity Tests ====================

    def test_merchant_data_types(self):
        """Test merchant data types"""
        list_response = self.http_client.get("/api/merchant/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        if merchant_list:
            merchant = merchant_list[0]
            assert isinstance(merchant.get("id"), (int, str)), "ID should be int or string"
            assert isinstance(merchant.get("name"), str), "Name should be string"
            if "rating" in merchant:
                assert isinstance(merchant["rating"], (int, float)), "Rating should be numeric"

    def test_merchant_rating_range(self):
        """Test merchant rating is within valid range"""
        list_response = self.http_client.get("/api/merchant/list")
        data = list_response.body.get("data", {})
        merchant_list = data.get("list", [])

        for merchant in merchant_list:
            if "rating" in merchant and merchant["rating"] is not None:
                assert 0 <= merchant["rating"] <= 5, "Rating should be between 0 and 5"
