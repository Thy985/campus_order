"""
Custom assertions for API testing framework.
Provides domain-specific assertion methods for API responses.
"""
from typing import Any, Dict, List, Optional, Union
import json

from utils.http_client import APIResponse
from utils.logger import get_logger

logger = get_logger()


class APIAssertions:
    """Custom assertions for API testing"""

    @staticmethod
    def assert_status_code(response: APIResponse, expected_code: int, message: Optional[str] = None):
        """Assert response status code"""
        msg = message or f"Expected status code {expected_code}, but got {response.status_code}"
        assert response.status_code == expected_code, msg

    @staticmethod
    def assert_success(response: APIResponse, message: Optional[str] = None):
        """Assert response is successful (2xx)"""
        msg = message or f"Expected success status code (2xx), but got {response.status_code}"
        assert response.is_success, msg

    @staticmethod
    def assert_client_error(response: APIResponse, message: Optional[str] = None):
        """Assert response is client error (4xx)"""
        msg = message or f"Expected client error status code (4xx), but got {response.status_code}"
        assert response.is_client_error, msg

    @staticmethod
    def assert_server_error(response: APIResponse, message: Optional[str] = None):
        """Assert response is server error (5xx)"""
        msg = message or f"Expected server error status code (5xx), but got {response.status_code}"
        assert response.is_server_error, msg

    @staticmethod
    def assert_response_time(response: APIResponse, max_time: float, message: Optional[str] = None):
        """Assert response time is within limit"""
        msg = message or f"Response time {response.response_time:.3f}s exceeded limit {max_time}s"
        assert response.response_time <= max_time, msg

    @staticmethod
    def assert_has_field(response: APIResponse, field: str, message: Optional[str] = None):
        """Assert response body has specific field"""
        body = response.body
        if not isinstance(body, dict):
            raise AssertionError(f"Response body is not a dict: {type(body)}")

        msg = message or f"Response body missing field: {field}"
        assert field in body, msg

    @staticmethod
    def assert_field_equals(response: APIResponse, field: str, expected_value: Any, message: Optional[str] = None):
        """Assert response field equals expected value"""
        APIAssertions.assert_has_field(response, field)
        actual_value = response.body[field]
        msg = message or f"Field '{field}' expected {expected_value}, but got {actual_value}"
        assert actual_value == expected_value, msg

    @staticmethod
    def assert_field_contains(response: APIResponse, field: str, expected_value: Any, message: Optional[str] = None):
        """Assert response field contains expected value"""
        APIAssertions.assert_has_field(response, field)
        actual_value = response.body[field]
        msg = message or f"Field '{field}' value {actual_value} does not contain {expected_value}"
        assert expected_value in actual_value, msg

    @staticmethod
    def assert_field_type(response: APIResponse, field: str, expected_type: type, message: Optional[str] = None):
        """Assert response field is of expected type"""
        APIAssertions.assert_has_field(response, field)
        actual_value = response.body[field]
        msg = message or f"Field '{field}' expected type {expected_type.__name__}, but got {type(actual_value).__name__}"
        assert isinstance(actual_value, expected_type), msg

    @staticmethod
    def assert_is_list(response: APIResponse, message: Optional[str] = None):
        """Assert response body is a list"""
        body = response.body
        msg = message or f"Response body is not a list: {type(body)}"
        assert isinstance(body, list), msg

    @staticmethod
    def assert_list_not_empty(response: APIResponse, message: Optional[str] = None):
        """Assert response body is a non-empty list"""
        APIAssertions.assert_is_list(response)
        msg = message or "Response list is empty"
        assert len(response.body) > 0, msg

    @staticmethod
    def assert_list_length(response: APIResponse, expected_length: int, message: Optional[str] = None):
        """Assert response list has expected length"""
        APIAssertions.assert_is_list(response)
        actual_length = len(response.body)
        msg = message or f"Expected list length {expected_length}, but got {actual_length}"
        assert actual_length == expected_length, msg

    @staticmethod
    def assert_has_pagination(response: APIResponse, message: Optional[str] = None):
        """Assert response has pagination fields"""
        required_fields = ['list', 'total', 'page', 'pageSize']
        for field in required_fields:
            APIAssertions.assert_has_field(response, field, message)

    @staticmethod
    def assert_no_sensitive_data(response: APIResponse, sensitive_fields: List[str] = None, message: Optional[str] = None):
        """Assert response does not contain sensitive data"""
        if sensitive_fields is None:
            sensitive_fields = ['password', 'secret', 'token', 'key', 'credential']

        body_str = json.dumps(response.body)
        for field in sensitive_fields:
            msg = message or f"Response contains sensitive field: {field}"
            assert field not in body_str.lower(), msg

    @staticmethod
    def assert_error_message(response: APIResponse, expected_message: Optional[str] = None, message: Optional[str] = None):
        """Assert response contains error message"""
        APIAssertions.assert_has_field(response, 'message')
        if expected_message:
            actual_message = response.body.get('message', '')
            msg = message or f"Expected error message '{expected_message}', but got '{actual_message}'"
            assert expected_message in actual_message, msg

    @staticmethod
    def assert_code_equals(response: APIResponse, expected_code: int, message: Optional[str] = None):
        """Assert response code equals expected value (business code, not HTTP status)"""
        APIAssertions.assert_has_field(response, 'code')
        actual_code = response.body.get('code')
        msg = message or f"Expected code {expected_code}, but got {actual_code}"
        assert actual_code == expected_code, msg


# Convenience functions for direct use
def assert_status_code(response: APIResponse, expected_code: int, message: Optional[str] = None):
    APIAssertions.assert_status_code(response, expected_code, message)


def assert_success(response: APIResponse, message: Optional[str] = None):
    APIAssertions.assert_success(response, message)


def assert_response_time(response: APIResponse, max_time: float, message: Optional[str] = None):
    APIAssertions.assert_response_time(response, max_time, message)


def assert_has_field(response: APIResponse, field: str, message: Optional[str] = None):
    APIAssertions.assert_has_field(response, field, message)


def assert_field_equals(response: APIResponse, field: str, expected_value: Any, message: Optional[str] = None):
    APIAssertions.assert_field_equals(response, field, expected_value, message)


def assert_error_message(response: APIResponse, expected_message: Optional[str] = None, message: Optional[str] = None):
    APIAssertions.assert_error_message(response, expected_message, message)


def assert_is_list(response: APIResponse, message: Optional[str] = None):
    APIAssertions.assert_is_list(response, message)


def assert_list_not_empty(response: APIResponse, message: Optional[str] = None):
    APIAssertions.assert_list_not_empty(response, message)


def assert_list_length(response: APIResponse, expected_length: int, message: Optional[str] = None):
    APIAssertions.assert_list_length(response, expected_length, message)


def assert_has_pagination(response: APIResponse, message: Optional[str] = None):
    APIAssertions.assert_has_pagination(response, message)


def assert_field_contains(response: APIResponse, field: str, expected_value: Any, message: Optional[str] = None):
    APIAssertions.assert_field_contains(response, field, expected_value, message)


def assert_field_type(response: APIResponse, field: str, expected_type: type, message: Optional[str] = None):
    APIAssertions.assert_field_type(response, field, expected_type, message)


def assert_no_sensitive_data(response: APIResponse, sensitive_fields: List[str] = None, message: Optional[str] = None):
    APIAssertions.assert_no_sensitive_data(response, sensitive_fields, message)


def assert_code_equals(response: APIResponse, expected_code: int, message: Optional[str] = None):
    APIAssertions.assert_code_equals(response, expected_code, message)
