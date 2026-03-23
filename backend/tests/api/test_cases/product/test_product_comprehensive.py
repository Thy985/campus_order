"""
Comprehensive Product Service Tests
商品服务综合测试

Tests for product endpoints including:
- Product list
- Product detail
- Product categories
- Product search
- Product recommendations
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestProductComprehensive:
    """Comprehensive product service test suite"""

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

    # ==================== Product List Tests ====================

    def test_get_product_list_success(self):
        """Test getting product list"""
        response = self.http_client.get("/api/product/list")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate pagination structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain list"
        assert "total" in data, "Response should contain total"

    def test_get_product_list_with_pagination(self):
        """Test product list pagination"""
        response = self.http_client.get("/api/product/list", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response, ["code", "message", "data"])

        data = response.body.get("data", {})
        assert "page" in data or "current" in data, "Response should contain page number"
        assert "pageSize" in data or "size" in data, "Response should contain page size"

    def test_get_product_list_by_category(self):
        """Test getting products by category"""
        response = self.http_client.get("/api/product/list", params={
            "categoryId": 1
        })

        self._validate_success_response(response)

    def test_get_product_list_by_merchant(self):
        """Test getting products by merchant"""
        response = self.http_client.get("/api/product/list", params={
            "merchantId": 1
        })

        self._validate_success_response(response)

    def test_get_product_list_with_price_filter(self):
        """Test product list with price filter"""
        response = self.http_client.get("/api/product/list", params={
            "minPrice": 10,
            "maxPrice": 100
        })

        self._validate_success_response(response)

    def test_get_product_list_with_sorting(self):
        """Test product list with sorting"""
        response = self.http_client.get("/api/product/list", params={
            "sortBy": "price",
            "sortOrder": "asc"
        })

        self._validate_success_response(response)

    # ==================== Product Detail Tests ====================

    def test_get_product_detail_success(self):
        """Test getting product detail"""
        # First get a product ID from list
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product_id = product_list[0].get("id")
            response = self.http_client.get(f"/api/product/{product_id}")

            self._validate_success_response(response, ["code", "message", "data"])

            # Validate product data structure
            product_data = response.body.get("data", {})
            assert "id" in product_data, "Product should have id"
            assert "name" in product_data, "Product should have name"
            assert "price" in product_data, "Product should have price"

    def test_get_product_detail_not_found(self):
        """Test getting non-existent product"""
        response = self.http_client.get("/api/product/999999")

        assert response.status_code in [404, 400], "Should return 404 for non-existent product"

    def test_get_product_detail_invalid_id(self):
        """Test getting product with invalid ID"""
        response = self.http_client.get("/api/product/invalid")

        assert response.status_code in [400, 404], "Should return 400 or 404 for invalid ID"

    # ==================== Product Categories Tests ====================

    def test_get_product_categories_success(self):
        """Test getting product categories"""
        response = self.http_client.get("/api/product/categories")

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

    def test_get_product_category_tree(self):
        """Test getting product category tree"""
        response = self.http_client.get("/api/product/categories/tree")

        self._validate_success_response(response)

    # ==================== Product Search Tests ====================

    def test_search_products_success(self):
        """Test searching products"""
        response = self.http_client.get("/api/product/search", params={
            "keyword": "奶茶"
        })

        self._validate_success_response(response, ["code", "message", "data"])

    def test_search_products_empty_keyword(self):
        """Test searching with empty keyword"""
        response = self.http_client.get("/api/product/search", params={
            "keyword": ""
        })

        # Should either return all products or error
        assert response.status_code in [200, 400]

    def test_search_products_no_results(self):
        """Test searching with keyword that has no results"""
        response = self.http_client.get("/api/product/search", params={
            "keyword": "xyznonexistent123"
        })

        self._validate_success_response(response)

        data = response.body.get("data", {})
        product_list = data.get("list", [])
        assert len(product_list) == 0, "Should return empty list for no results"

    # ==================== Product Recommendations Tests ====================

    def test_get_recommended_products(self):
        """Test getting recommended products"""
        response = self.http_client.get("/api/product/recommendations")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_get_popular_products(self):
        """Test getting popular products"""
        response = self.http_client.get("/api/product/popular")

        self._validate_success_response(response, ["code", "message", "data"])

    def test_get_new_products(self):
        """Test getting new products"""
        response = self.http_client.get("/api/product/new")

        self._validate_success_response(response, ["code", "message", "data"])

    # ==================== Product Reviews Tests ====================

    def test_get_product_reviews_success(self):
        """Test getting product reviews"""
        # First get a product ID
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product_id = product_list[0].get("id")
            response = self.http_client.get(f"/api/product/{product_id}/reviews")

            self._validate_success_response(response, ["code", "message", "data"])

    def test_get_product_reviews_with_pagination(self):
        """Test product reviews with pagination"""
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product_id = product_list[0].get("id")
            response = self.http_client.get(f"/api/product/{product_id}/reviews", params={
                "page": 1,
                "pageSize": 5
            })

            self._validate_success_response(response)

    # ==================== Favorite Product Tests ====================

    def test_add_product_to_favorites(self, authenticated_user):
        """Test adding product to favorites"""
        # First get a product ID
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product_id = product_list[0].get("id")
            response = self.http_client.post(f"/api/product/{product_id}/favorite")

            assert response.status_code in [200, 401]
            if response.is_success:
                self._validate_success_response(response)

    def test_get_favorite_products(self, authenticated_user):
        """Test getting favorite products list"""
        response = self.http_client.get("/api/user/favorites/products")

        assert response.status_code in [200, 401]
        if response.is_success:
            self._validate_success_response(response, ["code", "message", "data"])

    # ==================== Performance Tests ====================

    def test_product_list_response_time(self):
        """Test product list response time"""
        response = self.http_client.get("/api/product/list")
        self.assertions.assert_response_time(response, 1.5)

    def test_product_detail_response_time(self):
        """Test product detail response time"""
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product_id = product_list[0].get("id")
            response = self.http_client.get(f"/api/product/{product_id}")
            self.assertions.assert_response_time(response, 1.0)

    def test_product_search_response_time(self):
        """Test product search response time"""
        response = self.http_client.get("/api/product/search", params={
            "keyword": "奶茶"
        })
        self.assertions.assert_response_time(response, 2.0)

    # ==================== Data Integrity Tests ====================

    def test_product_data_types(self):
        """Test product data types"""
        list_response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            product = product_list[0]
            assert isinstance(product.get("id"), (int, str)), "ID should be int or string"
            assert isinstance(product.get("name"), str), "Name should be string"
            assert isinstance(product.get("price"), (int, float)), "Price should be numeric"

    def test_product_price_positive(self):
        """Test product price is positive"""
        list_response = self.http_client.get("/api/product/list")
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        for product in product_list:
            price = product.get("price")
            if price is not None:
                assert price >= 0, "Price should be non-negative"

    def test_product_stock_non_negative(self):
        """Test product stock is non-negative"""
        list_response = self.http_client.get("/api/product/list")
        data = list_response.body.get("data", {})
        product_list = data.get("list", [])

        for product in product_list:
            stock = product.get("stock")
            if stock is not None:
                assert stock >= 0, "Stock should be non-negative"
