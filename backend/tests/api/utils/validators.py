"""
Response validators for API testing framework.
Validates API responses against expected schemas and rules.
"""
from typing import Any, Dict, List, Optional, Callable
import jsonschema
from jsonschema import validate, ValidationError

from utils.http_client import APIResponse
from utils.logger import get_logger

logger = get_logger()


class ResponseValidator:
    """API response validator"""

    # Common JSON schemas
    USER_SCHEMA = {
        "type": "object",
        "required": ["id", "username", "nickname"],
        "properties": {
            "id": {"type": "integer"},
            "username": {"type": "string"},
            "nickname": {"type": "string"},
            "phone": {"type": "string"},
            "email": {"type": "string"},
            "avatar": {"type": "string"},
            "gender": {"type": "integer"},
            "status": {"type": "integer"},
            "createTime": {"type": "string"},
        }
    }

    MERCHANT_SCHEMA = {
        "type": "object",
        "required": ["id", "name", "categoryId"],
        "properties": {
            "id": {"type": "integer"},
            "name": {"type": "string"},
            "categoryId": {"type": "integer"},
            "phone": {"type": "string"},
            "address": {"type": "string"},
            "description": {"type": "string"},
            "logo": {"type": "string"},
            "rating": {"type": "number"},
            "avgPrice": {"type": "number"},
            "status": {"type": "integer"},
        }
    }

    PRODUCT_SCHEMA = {
        "type": "object",
        "required": ["id", "name", "price"],
        "properties": {
            "id": {"type": "integer"},
            "merchantId": {"type": "integer"},
            "categoryId": {"type": "integer"},
            "name": {"type": "string"},
            "description": {"type": "string"},
            "price": {"type": "number"},
            "originalPrice": {"type": "number"},
            "stock": {"type": "integer"},
            "unit": {"type": "string"},
            "image": {"type": "string"},
            "status": {"type": "integer"},
        }
    }

    ORDER_SCHEMA = {
        "type": "object",
        "required": ["id", "orderNo", "userId", "merchantId", "totalAmount", "status"],
        "properties": {
            "id": {"type": "integer"},
            "orderNo": {"type": "string"},
            "userId": {"type": "integer"},
            "merchantId": {"type": "integer"},
            "totalAmount": {"type": "number"},
            "payAmount": {"type": "number"},
            "deliveryFee": {"type": "number"},
            "discountAmount": {"type": "number"},
            "status": {"type": "integer"},
            "payStatus": {"type": "integer"},
            "createTime": {"type": "string"},
            "items": {
                "type": "array",
                "items": {
                    "type": "object",
                    "properties": {
                        "id": {"type": "integer"},
                        "productId": {"type": "integer"},
                        "productName": {"type": "string"},
                        "quantity": {"type": "integer"},
                        "price": {"type": "number"},
                    }
                }
            }
        }
    }

    PAGINATION_SCHEMA = {
        "type": "object",
        "required": ["list", "total", "page", "pageSize"],
        "properties": {
            "list": {"type": "array"},
            "total": {"type": "integer"},
            "page": {"type": "integer"},
            "pageSize": {"type": "integer"},
            "totalPages": {"type": "integer"},
        }
    }

    @staticmethod
    def validate_json_schema(data: Any, schema: Dict[str, Any]) -> bool:
        """Validate data against JSON schema"""
        try:
            validate(instance=data, schema=schema)
            return True
        except ValidationError as e:
            logger.error(f"Schema validation failed: {e.message}")
            return False

    @staticmethod
    def validate_response_schema(response: APIResponse, schema: Dict[str, Any]) -> bool:
        """Validate response body against schema"""
        return ResponseValidator.validate_json_schema(response.body, schema)

    @staticmethod
    def validate_user_response(response: APIResponse) -> bool:
        """Validate user response"""
        return ResponseValidator.validate_response_schema(response, ResponseValidator.USER_SCHEMA)

    @staticmethod
    def validate_merchant_response(response: APIResponse) -> bool:
        """Validate merchant response"""
        return ResponseValidator.validate_response_schema(response, ResponseValidator.MERCHANT_SCHEMA)

    @staticmethod
    def validate_product_response(response: APIResponse) -> bool:
        """Validate product response"""
        return ResponseValidator.validate_response_schema(response, ResponseValidator.PRODUCT_SCHEMA)

    @staticmethod
    def validate_order_response(response: APIResponse) -> bool:
        """Validate order response"""
        return ResponseValidator.validate_response_schema(response, ResponseValidator.ORDER_SCHEMA)

    @staticmethod
    def validate_pagination_response(response: APIResponse, item_schema: Optional[Dict[str, Any]] = None) -> bool:
        """Validate pagination response"""
        if not ResponseValidator.validate_response_schema(response, ResponseValidator.PAGINATION_SCHEMA):
            return False

        if item_schema:
            list_data = response.body.get('list', [])
            for item in list_data:
                if not ResponseValidator.validate_json_schema(item, item_schema):
                    return False

        return True

    @staticmethod
    def validate_list_response(response: APIResponse, item_schema: Optional[Dict[str, Any]] = None) -> bool:
        """Validate list response"""
        if not isinstance(response.body, list):
            logger.error(f"Response body is not a list: {type(response.body)}")
            return False

        if item_schema:
            for item in response.body:
                if not ResponseValidator.validate_json_schema(item, item_schema):
                    return False

        return True


class FieldValidator:
    """Field-level validators"""

    @staticmethod
    def is_valid_phone(phone: str) -> bool:
        """Validate Chinese mobile phone number"""
        import re
        pattern = r'^1[3-9]\d{9}$'
        return bool(re.match(pattern, phone))

    @staticmethod
    def is_valid_email(email: str) -> bool:
        """Validate email address"""
        import re
        pattern = r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$'
        return bool(re.match(pattern, email))

    @staticmethod
    def is_positive_number(value: Any) -> bool:
        """Validate positive number"""
        try:
            num = float(value)
            return num > 0
        except (TypeError, ValueError):
            return False

    @staticmethod
    def is_non_empty_string(value: Any) -> bool:
        """Validate non-empty string"""
        return isinstance(value, str) and len(value.strip()) > 0

    @staticmethod
    def is_valid_id(value: Any) -> bool:
        """Validate ID (positive integer)"""
        try:
            id_val = int(value)
            return id_val > 0
        except (TypeError, ValueError):
            return False

    @staticmethod
    def is_within_range(value: Any, min_val: float, max_val: float) -> bool:
        """Validate value is within range"""
        try:
            num = float(value)
            return min_val <= num <= max_val
        except (TypeError, ValueError):
            return False


# Convenience functions
def validate_schema(data: Any, schema: Dict[str, Any]) -> bool:
    return ResponseValidator.validate_json_schema(data, schema)


def validate_user(response: APIResponse) -> bool:
    return ResponseValidator.validate_user_response(response)


def validate_merchant(response: APIResponse) -> bool:
    return ResponseValidator.validate_merchant_response(response)


def validate_product(response: APIResponse) -> bool:
    return ResponseValidator.validate_product_response(response)


def validate_order(response: APIResponse) -> bool:
    return ResponseValidator.validate_order_response(response)


def validate_pagination(response: APIResponse, item_schema: Optional[Dict[str, Any]] = None) -> bool:
    return ResponseValidator.validate_pagination_response(response, item_schema)
