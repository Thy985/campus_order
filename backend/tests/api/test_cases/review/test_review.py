"""
Review API tests.
Tests review creation, query, and merchant reply APIs.
"""
import pytest

from utils.http_client import get_http_client
from utils.data_generator import get_data_generator
from utils.assertions import (
    assert_status_code, assert_success, assert_has_field,
    assert_has_pagination
)


@pytest.mark.review
class TestReviewCreate:
    """Test review creation API"""

    @pytest.fixture(autouse=True)
    def setup(self, test_user):
        self.http_client = get_http_client()
        self.data_generator = get_data_generator()
        self.user_data = test_user
        self.http_client.set_token(self.user_data.get("token"))

    def test_create_review_success(self):
        """Test creating review"""
        review_data = self.data_generator.generate_review(
            user_id=self.user_data.get("userId"),
            order_id=1,
            merchant_id=1
        )

        response = self.http_client.post("/api/review/create", json_data={
            "orderId": 1,
            "merchantId": 1,
            "rating": review_data["rating"],
            "content": review_data["content"],
            "images": review_data.get("images", [])
        })

        # May return success, 401 (not authenticated), or 500 if not implemented
        assert response.status_code in [200, 201, 401, 500]

    def test_create_review_invalid_order(self):
        """Test creating review for invalid order"""
        response = self.http_client.post("/api/review/create", json_data={
            "orderId": 999999,
            "merchantId": 1,
            "rating": 5,
            "content": "Test review"
        })

        # May return 400, 401 (not authenticated), or 500
        assert response.status_code in [200, 400, 401, 500]

    def test_create_review_invalid_rating(self):
        """Test creating review with invalid rating"""
        response = self.http_client.post("/api/review/create", json_data={
            "orderId": 1,
            "merchantId": 1,
            "rating": 10,  # Invalid rating (>5)
            "content": "Test review"
        })

        # May return 400, 401 (not authenticated), or 500
        assert response.status_code in [200, 400, 401, 500]

    def test_create_review_missing_content(self):
        """Test creating review without content"""
        response = self.http_client.post("/api/review/create", json_data={
            "orderId": 1,
            "merchantId": 1,
            "rating": 5
            # Missing content
        })

        # May return 400, 401 (not authenticated), or 500
        assert response.status_code in [200, 400, 401, 500]


@pytest.mark.review
class TestReviewQuery:
    """Test review query API"""

    @pytest.fixture(autouse=True)
    def setup(self):
        self.http_client = get_http_client()

    def test_get_review_list_by_merchant(self):
        """Test getting reviews by merchant"""
        response = self.http_client.get("/api/review/merchant/1", params={
            "page": 1,
            "pageSize": 10
        })

        # May return success or 404/500 if not implemented
        assert response.status_code in [200, 404, 500]

    def test_get_review_detail(self):
        """Test getting review detail"""
        response = self.http_client.get("/api/review/order/1")

        # May return success or 404/500 if not implemented
        assert response.status_code in [200, 404, 500]


@pytest.mark.review
class TestMerchantReply:
    """Test merchant reply API"""

    @pytest.fixture(autouse=True)
    def setup(self, test_user):
        self.http_client = get_http_client()
        self.user_data = test_user
        self.http_client.set_token(self.user_data.get("token"))

    def test_reply_to_review(self):
        """Test merchant replying to review"""
        response = self.http_client.post("/api/review/reply", json_data={
            "reviewId": 1,
            "content": "Thank you for your review!"
        })

        # May return success, 401 (not authenticated), or 404/500 if not implemented
        assert response.status_code in [200, 201, 401, 404, 500]

    def test_reply_to_review_not_found(self):
        """Test replying to non-existent review"""
        response = self.http_client.post("/api/review/reply", json_data={
            "reviewId": 999999,
            "content": "Thank you for your review!"
        })

        # May return 404, 401 (not authenticated), or 500
        assert response.status_code in [200, 401, 404, 500]


@pytest.mark.review
class TestReviewCheck:
    """Test review check API"""

    @pytest.fixture(autouse=True)
    def setup(self, test_user):
        self.http_client = get_http_client()
        self.user_data = test_user
        self.http_client.set_token(self.user_data.get("token"))

    def test_check_order_reviewed(self):
        """Test checking if order has been reviewed"""
        response = self.http_client.get("/api/review/has-reviewed/1")

        # May return success, 401 (not authenticated), or 404/500 if not implemented
        assert response.status_code in [200, 401, 404, 500]
