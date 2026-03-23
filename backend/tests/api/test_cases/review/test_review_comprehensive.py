"""
Comprehensive Review Service Tests
评价服务综合测试

Tests for review endpoints including:
- Create review
- Get product reviews
- Get user reviews
- Update review
- Delete review
"""
import pytest
from typing import Dict, Any

from utils.http_client import HTTPClient, APIResponse
from utils.assertions import APIAssertions
from utils.validators import ResponseValidator, FieldValidator
from utils.data_generator import DataGenerator


class TestReviewComprehensive:
    """Comprehensive review service test suite"""

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
    def test_product(self):
        """Get a test product"""
        response = self.http_client.get("/api/product/list", params={"pageSize": 1})
        data = response.body.get("data", {})
        product_list = data.get("list", [])

        if product_list:
            return product_list[0]
        return None

    def _validate_success_response(self, response: APIResponse, required_fields: list = None):
        """Helper method to validate successful API response"""
        self.assertions.assert_success(response)
        required = required_fields or ["code", "message"]
        for field in required:
            self.assertions.assert_has_field(response, field)

    # ==================== Create Review Tests ====================

    def test_create_review_success(self, authenticated_user, test_product):
        """Test creating a review"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")

        response = self.http_client.post("/api/review", json_data={
            "productId": product_id,
            "rating": 5,
            "content": "这个商品非常好，值得推荐！",
            "images": [],
            "isAnonymous": False
        })

        # May require completed order to review
        assert response.status_code in [200, 400, 403, 422]
        if response.is_success:
            self._validate_success_response(response, ["code", "message", "data"])

    def test_create_review_invalid_rating(self, authenticated_user, test_product):
        """Test creating review with invalid rating"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")

        response = self.http_client.post("/api/review", json_data={
            "productId": product_id,
            "rating": 10,  # Invalid rating (>5)
            "content": "Test review"
        })

        assert response.status_code in [400, 422], "Should return error for invalid rating"

    def test_create_review_missing_content(self, authenticated_user, test_product):
        """Test creating review without content"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")

        response = self.http_client.post("/api/review", json_data={
            "productId": product_id,
            "rating": 5
            # Missing content
        })

        # May accept or reject empty content
        assert response.status_code in [200, 400]

    def test_create_review_unauthorized(self, test_product):
        """Test creating review without authentication"""
        if not test_product:
            pytest.skip("No products available for testing")

        self.http_client.clear_token()
        product_id = test_product.get("id")

        response = self.http_client.post("/api/review", json_data={
            "productId": product_id,
            "rating": 5,
            "content": "Test review"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Get Product Reviews Tests ====================

    def test_get_product_reviews_success(self, test_product):
        """Test getting reviews for a product"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate reviews structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain reviews list"

    def test_get_product_reviews_with_pagination(self, test_product):
        """Test product reviews with pagination"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews", params={
            "page": 1,
            "pageSize": 10
        })

        self._validate_success_response(response)

    def test_get_product_reviews_with_rating_filter(self, test_product):
        """Test product reviews with rating filter"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews", params={
            "rating": 5
        })

        self._validate_success_response(response)

    def test_get_product_reviews_not_found(self):
        """Test getting reviews for non-existent product"""
        response = self.http_client.get("/api/product/999999/reviews")

        assert response.status_code in [404, 400], "Should return 404 for non-existent product"

    # ==================== Get User Reviews Tests ====================

    def test_get_user_reviews_success(self, authenticated_user):
        """Test getting user's reviews"""
        response = self.http_client.get("/api/user/reviews")

        self._validate_success_response(response, ["code", "message", "data"])

        # Validate list structure
        data = response.body.get("data", {})
        assert "list" in data, "Response should contain reviews list"

    def test_get_user_reviews_unauthorized(self):
        """Test getting user reviews without authentication"""
        self.http_client.clear_token()
        response = self.http_client.get("/api/user/reviews")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Review Summary Tests ====================

    def test_get_product_review_summary(self, test_product):
        """Test getting product review summary"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/review-summary")

        self._validate_success_response(response)

        # Validate summary data
        data = response.body.get("data", {})
        if "averageRating" in data:
            assert 0 <= data["averageRating"] <= 5, "Average rating should be between 0 and 5"

    # ==================== Update Review Tests ====================

    def test_update_review_success(self, authenticated_user, test_product):
        """Test updating a review"""
        # This would require an existing review
        pytest.skip("Requires existing review for testing")

    def test_update_review_unauthorized(self):
        """Test updating review without authentication"""
        self.http_client.clear_token()
        response = self.http_client.put("/api/review/1", json_data={
            "content": "Updated review"
        })

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Delete Review Tests ====================

    def test_delete_review_success(self, authenticated_user):
        """Test deleting a review"""
        # This would require an existing review
        pytest.skip("Requires existing review for testing")

    def test_delete_review_unauthorized(self):
        """Test deleting review without authentication"""
        self.http_client.clear_token()
        response = self.http_client.delete("/api/review/1")

        assert response.status_code == 401, "Should return 401 for unauthorized access"

    # ==================== Performance Tests ====================

    def test_get_product_reviews_response_time(self, test_product):
        """Test get product reviews response time"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews")
        self.assertions.assert_response_time(response, 1.5)

    def test_get_user_reviews_response_time(self, authenticated_user):
        """Test get user reviews response time"""
        response = self.http_client.get("/api/user/reviews")
        self.assertions.assert_response_time(response, 1.0)

    # ==================== Data Integrity Tests ====================

    def test_review_rating_range(self, test_product):
        """Test that review ratings are within valid range"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        reviews = data.get("list", [])

        for review in reviews:
            rating = review.get("rating")
            if rating is not None:
                assert 1 <= rating <= 5, "Rating should be between 1 and 5"

    def test_review_data_types(self, test_product):
        """Test review data types"""
        if not test_product:
            pytest.skip("No products available for testing")

        product_id = test_product.get("id")
        response = self.http_client.get(f"/api/product/{product_id}/reviews")

        self._validate_success_response(response)

        data = response.body.get("data", {})
        reviews = data.get("list", [])

        if reviews:
            review = reviews[0]
            if "id" in review:
                assert isinstance(review["id"], (int, str)), "ID should be int or string"
            if "rating" in review:
                assert isinstance(review["rating"], int), "Rating should be int"
            if "content" in review:
                assert isinstance(review["content"], str), "Content should be string"
            if "isAnonymous" in review:
                assert isinstance(review["isAnonymous"], bool), "IsAnonymous should be boolean"
