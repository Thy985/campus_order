"""
Test Suites Configuration - 使用正确的API路径和参数
根据实际Controller代码调整
"""

# 测试账号配置
TEST_ACCOUNTS = {
    "admin": {
        "phone": "13800000001",
        "email": "admin@campus.edu",
        "password": "123456",
        "user_type": 2
    },
    "student1": {
        "phone": "13800000011",
        "email": "student01@campus.edu",
        "password": "123456",
        "user_type": 0
    }
}

# 经过验证正常工作的测试套件 - 使用正确的API路径
TEST_SUITES = {
    "health": {
        "name": "Health Check Tests",
        "description": "Basic health check and connectivity tests",
        "tests": [
            {
                "name": "health_check",
                "method": "GET",
                "endpoint": "/actuator/health",
                "description": "Test system health endpoint",
                "expected_status": 200,
                "max_response_time": 10.0,
                "priority": "critical",
                "tags": ["health", "monitoring"]
            },
            {
                "name": "api_root",
                "method": "GET",
                "endpoint": "/",
                "description": "Test API root endpoint",
                "expected_status": 200,
                "priority": "low",
                "tags": ["health", "root"]
            }
        ]
    },
    "auth": {
        "name": "Authentication API Tests",
        "description": "Tests for authentication endpoints",
        "tests": [

            {
                "name": "login_invalid_password",
                "method": "POST",
                "endpoint": "/api/auth/login",
                "description": "Test login with invalid password returns error",
                "expected_status": 400,
                "required_fields": ["code", "message"],
                "business_checks": {
                    "code": lambda x: x == 20002
                },
                "request_body": {
                    "phone": "13800000011",
                    "password": "wrongpassword"
                },
                "priority": "high",
                "tags": ["auth", "login", "negative"]
            },
            {
                "name": "login_nonexistent_user",
                "method": "POST",
                "endpoint": "/api/auth/login",
                "description": "Test login with non-existent user returns error",
                "expected_status": 400,
                "required_fields": ["code", "message"],
                "request_body": {
                    "phone": "13999999999",
                    "password": "123456"
                },
                "priority": "high",
                "tags": ["auth", "login", "negative"]
            },

            {
                "name": "get_verify_code_phone",
                "method": "POST",
                "endpoint": "/api/auth/verify-code",
                "description": "Test getting verification code by phone",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "request_body": {
                    "phone": "13800000011"
                },
                "priority": "medium",
                "tags": ["auth", "verify-code", "phone"]
            },
            {
                "name": "forgot_password_expired_code",
                "method": "POST",
                "endpoint": "/api/auth/forgot-password",
                "description": "Test forgot password with expired/invalid code",
                "expected_status": 400,
                "required_fields": ["code", "message"],
                "business_checks": {
                    "code": lambda x: x in [20006, 20007]
                },
                "request_body": {
                    "email": "student01@campus.edu",
                    "verifyCode": "000000",
                    "newPassword": "newpassword123"
                },
                "priority": "medium",
                "tags": ["auth", "forgot-password", "negative"]
            },
            {
                "name": "get_current_user_unauthorized",
                "method": "GET",
                "endpoint": "/api/auth/current",
                "description": "Test getting current user without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["auth", "current", "unauthorized"]
            }
        ]
    },
    "merchant": {
        "name": "Merchant API Tests",
        "description": "Tests for merchant endpoints",
        "tests": [
            {
                "name": "list_merchants",
                "method": "GET",
                "endpoint": "/api/merchant/list",
                "description": "Test listing merchants",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["merchant", "list", "public"]
            },
            {
                "name": "list_merchants_with_pagination",
                "method": "GET",
                "endpoint": "/api/merchant/list?page=1&pageSize=5",
                "description": "Test listing merchants with pagination",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["merchant", "list", "pagination"]
            },
            {
                "name": "search_merchants",
                "method": "GET",
                "endpoint": "/api/merchant/search?keyword=餐厅&page=1&pageSize=10",
                "description": "Test searching merchants by keyword",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["merchant", "search"]
            },
            {
                "name": "get_merchant_detail",
                "method": "GET",
                "endpoint": "/api/merchant/detail/1",
                "description": "Test getting merchant details by ID",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["merchant", "detail", "public"]
            },

            {
                "name": "get_merchant_category_list",
                "method": "GET",
                "endpoint": "/api/merchant/category/list",
                "description": "Test listing merchant categories",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["merchant", "category", "public"]
            },
            {
                "name": "get_hot_merchants",
                "method": "GET",
                "endpoint": "/api/merchant/hot?limit=5",
                "description": "Test getting hot merchants",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["merchant", "hot"]
            },
            {
                "name": "get_nearby_merchants",
                "method": "GET",
                "endpoint": "/api/merchant/nearby?latitude=31.2304&longitude=121.4737&distance=5000",
                "description": "Test getting nearby merchants",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["merchant", "nearby"]
            },
            {
                "name": "get_products_by_merchant",
                "method": "GET",
                "endpoint": "/api/merchant/products?merchantId=1",
                "description": "Test getting products by merchant ID",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["merchant", "products"]
            },
            {
                "name": "get_merchant_product_categories",
                "method": "GET",
                "endpoint": "/api/merchant/products/categories?merchantId=1",
                "description": "Test getting product categories by merchant",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["merchant", "categories"]
            }
        ]
    },
    "product": {
        "name": "Product API Tests",
        "description": "Tests for product endpoints",
        "tests": [
            {
                "name": "get_product_detail",
                "method": "GET",
                "endpoint": "/api/product/detail/1",
                "description": "Test getting product details by ID",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["product", "detail", "public"]
            },

            {
                "name": "get_product_list_by_merchant",
                "method": "GET",
                "endpoint": "/api/product/list?merchantId=1&page=1&pageSize=10",
                "description": "Test listing products by merchant with required params",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["product", "list", "public"]
            },
            {
                "name": "search_products",
                "method": "GET",
                "endpoint": "/api/product/search?keyword=米饭&page=1&pageSize=10",
                "description": "Test searching products by keyword",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["product", "search"]
            },
            {
                "name": "get_hot_products",
                "method": "GET",
                "endpoint": "/api/product/hot?merchantId=1&limit=5",
                "description": "Test getting hot products",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["product", "hot"]
            },
            {
                "name": "get_products_by_merchant_id",
                "method": "GET",
                "endpoint": "/api/product/merchant/1",
                "description": "Test getting products by merchant ID (path variable)",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "high",
                "tags": ["product", "list", "merchant"]
            }
        ]
    },
    "category": {
        "name": "Category API Tests",
        "description": "Tests for category endpoints",
        "tests": [
            {
                "name": "list_product_categories",
                "method": "GET",
                "endpoint": "/api/product/category/list",
                "description": "Test listing product categories",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["category", "list", "public"]
            }
        ]
    },
    "coupon": {
        "name": "Coupon API Tests",
        "description": "Tests for coupon endpoints",
        "tests": [
            {
                "name": "list_coupons",
                "method": "GET",
                "endpoint": "/api/coupon/list",
                "description": "Test listing coupons",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["coupon", "list", "public"]
            },
            {
                "name": "get_my_coupons_unauthorized",
                "method": "GET",
                "endpoint": "/api/coupon/my",
                "description": "Test getting my coupons without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["coupon", "my", "auth-required"]
            },
            {
                "name": "receive_coupon_unauthorized",
                "method": "POST",
                "endpoint": "/api/coupon/receive/1",
                "description": "Test receiving coupon without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["coupon", "receive", "auth-required"]
            },
            {
                "name": "get_available_coupons_unauthorized",
                "method": "GET",
                "endpoint": "/api/coupon/available",
                "description": "Test getting available coupons without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["coupon", "available", "auth-required"]
            }
        ]
    },
    "order": {
        "name": "Order API Tests",
        "description": "Tests for order endpoints",
        "tests": [
            {
                "name": "create_order_unauthorized",
                "method": "POST",
                "endpoint": "/api/order",
                "description": "Test creating order without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "merchantId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                    ],
                    "deliveryAddress": "测试地址",
                    "contactPhone": "13800000011",
                    "contactName": "测试用户"
                },
                "priority": "high",
                "tags": ["order", "create", "auth-required"]
            },
            {
                "name": "get_order_list_unauthorized",
                "method": "GET",
                "endpoint": "/api/order/list",
                "description": "Test getting order list without login (should fail)",
                "expected_status": 401,
                "priority": "high",
                "tags": ["order", "list", "auth-required"]
            },
            {
                "name": "get_order_detail_unauthorized",
                "method": "GET",
                "endpoint": "/api/order/1",
                "description": "Test getting order detail without login (should fail)",
                "expected_status": 401,
                "priority": "high",
                "tags": ["order", "detail", "auth-required"]
            },
            {
                "name": "cancel_order_unauthorized",
                "method": "POST",
                "endpoint": "/api/order/1/cancel",
                "description": "Test canceling order without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["order", "cancel", "auth-required"]
            },
            {
                "name": "confirm_pickup_unauthorized",
                "method": "POST",
                "endpoint": "/api/order/1/confirm-pickup",
                "description": "Test confirming pickup without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["order", "confirm", "auth-required"]
            }
        ]
    },
    "address": {
        "name": "Address API Tests",
        "description": "Tests for address endpoints",
        "tests": [
            {
                "name": "get_address_list_unauthorized",
                "method": "GET",
                "endpoint": "/api/address/list",
                "description": "Test getting address list without login (should fail)",
                "expected_status": 401,
                "priority": "high",
                "tags": ["address", "list", "auth-required"]
            },
            {
                "name": "create_address_unauthorized",
                "method": "POST",
                "endpoint": "/api/address/create",
                "description": "Test creating address without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "name": "测试地址",
                    "phone": "13800000011",
                    "address": "测试详细地址",
                    "isDefault": True
                },
                "priority": "high",
                "tags": ["address", "create", "auth-required"]
            },
            {
                "name": "get_default_address_unauthorized",
                "method": "GET",
                "endpoint": "/api/address/default",
                "description": "Test getting default address without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["address", "default", "auth-required"]
            }
        ]
    },
    "review": {
        "name": "Review API Tests",
        "description": "Tests for review endpoints",
        "tests": [
            {
                "name": "get_merchant_reviews",
                "method": "GET",
                "endpoint": "/api/review/merchant/1",
                "description": "Test getting merchant reviews",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["review", "merchant", "public"]
            },
            {
                "name": "get_merchant_reviews_with_pagination",
                "method": "GET",
                "endpoint": "/api/review/merchant/1?page=1&size=5",
                "description": "Test getting merchant reviews with pagination",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["review", "merchant", "pagination"]
            },
            {
                "name": "get_review_by_order_id",
                "method": "GET",
                "endpoint": "/api/review/order/1",
                "description": "Test getting review by order ID",
                "expected_status": 200,
                "required_fields": ["code", "message", "data"],
                "business_checks": {
                    "code": lambda x: x == 200
                },
                "priority": "medium",
                "tags": ["review", "order"]
            },
            {
                "name": "create_review_unauthorized",
                "method": "POST",
                "endpoint": "/api/review/create",
                "description": "Test creating review without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "orderId": 1,
                    "merchantId": 1,
                    "rating": 5,
                    "content": "测试评价"
                },
                "priority": "medium",
                "tags": ["review", "create", "auth-required"]
            },
            {
                "name": "has_reviewed_unauthorized",
                "method": "GET",
                "endpoint": "/api/review/has-reviewed/1",
                "description": "Test checking if reviewed without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["review", "has-reviewed", "auth-required"]
            },
            {
                "name": "reply_review_unauthorized",
                "method": "POST",
                "endpoint": "/api/review/reply",
                "description": "Test replying review without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "reviewId": 1,
                    "reply": "测试回复"
                },
                "priority": "low",
                "tags": ["review", "reply", "auth-required"]
            }
        ]
    },
    "user": {
        "name": "User API Tests",
        "description": "Tests for user endpoints",
        "tests": [
            {
                "name": "get_profile_unauthorized",
                "method": "GET",
                "endpoint": "/api/user/profile",
                "description": "Test getting profile without login (should fail)",
                "expected_status": 401,
                "priority": "high",
                "tags": ["user", "profile", "auth-required"]
            },
            {
                "name": "update_profile_unauthorized",
                "method": "PUT",
                "endpoint": "/api/user/profile",
                "description": "Test updating profile without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "nickname": "新昵称"
                },
                "priority": "medium",
                "tags": ["user", "profile", "update", "auth-required"]
            },
            {
                "name": "update_password_unauthorized",
                "method": "PUT",
                "endpoint": "/api/user/password",
                "description": "Test updating password without login (should fail)",
                "expected_status": 401,
                "request_body": {
                    "oldPassword": "123456",
                    "newPassword": "newpassword"
                },
                "priority": "medium",
                "tags": ["user", "password", "auth-required"]
            },
            {
                "name": "get_user_info_unauthorized",
                "method": "GET",
                "endpoint": "/api/user/info",
                "description": "Test getting user info without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["user", "info", "auth-required"]
            },
            {
                "name": "delete_account_unauthorized",
                "method": "DELETE",
                "endpoint": "/api/user/account",
                "description": "Test deleting account without login (should fail)",
                "expected_status": 401,
                "priority": "low",
                "tags": ["user", "delete", "auth-required"]
            }
        ]
    },
    "notification": {
        "name": "Notification API Tests",
        "description": "Tests for notification endpoints",
        "tests": [
            {
                "name": "get_notifications_unauthorized",
                "method": "GET",
                "endpoint": "/api/notification",
                "description": "Test getting notifications without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["notification", "list", "auth-required"]
            },
            {
                "name": "get_unread_count_unauthorized",
                "method": "GET",
                "endpoint": "/api/notification/unread-count",
                "description": "Test getting unread count without login (should fail)",
                "expected_status": 401,
                "priority": "medium",
                "tags": ["notification", "unread", "auth-required"]
            },
            {
                "name": "mark_as_read_unauthorized",
                "method": "PUT",
                "endpoint": "/api/notification/1/read",
                "description": "Test marking as read without login (should fail)",
                "expected_status": 401,
                "priority": "low",
                "tags": ["notification", "read", "auth-required"]
            },
            {
                "name": "mark_all_as_read_unauthorized",
                "method": "PUT",
                "endpoint": "/api/notification/read-all",
                "description": "Test marking all as read without login (should fail)",
                "expected_status": 401,
                "priority": "low",
                "tags": ["notification", "read-all", "auth-required"]
            },
            {
                "name": "delete_notification_unauthorized",
                "method": "DELETE",
                "endpoint": "/api/notification/delete/1",
                "description": "Test deleting notification without login (should fail)",
                "expected_status": 401,
                "priority": "low",
                "tags": ["notification", "delete", "auth-required"]
            }
        ]
    }
}


def get_test_account(account_type: str) -> dict:
    """获取测试账号信息"""
    return TEST_ACCOUNTS.get(account_type, {})


def get_all_test_suites() -> dict:
    """获取所有测试套件"""
    return TEST_SUITES.copy()
