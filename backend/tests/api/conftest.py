"""
Pytest configuration and fixtures for API testing framework.
"""
import pytest
from typing import Generator

from utils.http_client import HTTPClient, get_http_client, reset_http_client
from utils.data_generator import DataGenerator, get_data_generator
from utils.logger import get_logger

logger = get_logger()


@pytest.fixture(scope="session")
def http_client() -> Generator[HTTPClient, None, None]:
    """HTTP client fixture"""
    client = get_http_client()
    yield client
    client.close()


@pytest.fixture(scope="function")
def fresh_http_client() -> Generator[HTTPClient, None, None]:
    """Fresh HTTP client fixture for each test"""
    reset_http_client()
    client = get_http_client()
    yield client
    client.close()
    reset_http_client()


@pytest.fixture(scope="session")
def data_generator() -> DataGenerator:
    """Data generator fixture"""
    return get_data_generator()


@pytest.fixture(scope="function")
def test_user(data_generator, http_client):
    """Create a test user and return user data with token"""
    user_data = data_generator.generate_user()

    # Register user
    response = http_client.post("/api/auth/register", json_data={
        "username": user_data["username"],
        "phone": user_data["phone"],
        "password": user_data["password"],
        "verifyCode": "123456"  # Test verification code
    })

    if response.status_code == 200:
        # Login to get token
        login_response = http_client.post("/api/auth/login", json_data={
            "phone": user_data["phone"],
            "password": user_data["password"]
        })

        if login_response.is_success:
            token = login_response.body.get("data", {}).get("token")
            http_client.set_token(token)
            user_data["token"] = token
            user_data["userId"] = login_response.body.get("data", {}).get("userId")

    yield user_data

    # Cleanup: Delete test user (if API available)
    # http_client.delete(f"/api/user/{user_data.get('userId')}")


@pytest.fixture(scope="function")
def auth_token(http_client, test_user):
    """Get authentication token"""
    return test_user.get("token")


@pytest.fixture(scope="function")
def authenticated_client(http_client, auth_token):
    """HTTP client with authentication"""
    http_client.set_token(auth_token)
    yield http_client
    http_client.clear_token()


@pytest.fixture(scope="session", autouse=True)
def setup_test_environment():
    """Setup test environment"""
    logger.info("Setting up test environment...")
    yield
    logger.info("Tearing down test environment...")
