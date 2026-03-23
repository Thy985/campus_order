"""
Test data generator for API testing framework.
Generates realistic test data using Faker library.
"""
import random
import string
from datetime import datetime, timedelta
from typing import Dict, Any, List, Optional

from faker import Faker

from config.settings import get_settings
from utils.logger import get_logger

logger = get_logger()
faker = Faker('zh_CN')


class DataGenerator:
    """Test data generator"""

    def __init__(self):
        self.settings = get_settings()
        self.user_prefix = self.settings.test_data.user_prefix
        self.user_password = self.settings.test_data.user_password
        self.merchant_prefix = self.settings.test_data.merchant_prefix
        self.product_prefix = self.settings.test_data.product_prefix

    def generate_phone(self) -> str:
        """Generate a valid Chinese mobile phone number"""
        prefixes = ['138', '139', '135', '136', '137', '150', '151', '152', '157', '158', '159']
        prefix = random.choice(prefixes)
        suffix = ''.join(random.choices(string.digits, k=8))
        return prefix + suffix

    def generate_username(self, prefix: Optional[str] = None) -> str:
        """Generate a unique username"""
        prefix = prefix or self.user_prefix
        timestamp = datetime.now().strftime('%m%d%H%M%S')
        random_suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=4))
        return f"{prefix}{timestamp}_{random_suffix}"

    def generate_nickname(self) -> str:
        """Generate a nickname"""
        return faker.name()

    def generate_email(self, username: Optional[str] = None) -> str:
        """Generate an email address"""
        if username:
            return f"{username}@test.com"
        return faker.email()

    def generate_password(self, length: int = 12) -> str:
        """Generate a strong password"""
        chars = string.ascii_letters + string.digits + "!@#$%^&*"
        return ''.join(random.choices(chars, k=length))

    def generate_user(self, **kwargs) -> Dict[str, Any]:
        """Generate user data"""
        username = kwargs.get('username') or self.generate_username()
        user_data = {
            'username': username,
            'nickname': kwargs.get('nickname') or self.generate_nickname(),
            'phone': kwargs.get('phone') or self.generate_phone(),
            'email': kwargs.get('email') or self.generate_email(username),
            'password': kwargs.get('password') or self.user_password,
            'avatar': kwargs.get('avatar') or faker.image_url(),
            'gender': kwargs.get('gender', random.choice([0, 1, 2])),  # 0: unknown, 1: male, 2: female
        }
        return user_data

    def generate_address(self, user_id: Optional[int] = None, **kwargs) -> Dict[str, Any]:
        """Generate address data"""
        return {
            'userId': user_id,
            'receiverName': kwargs.get('receiver_name') or faker.name(),
            'receiverPhone': kwargs.get('receiver_phone') or self.generate_phone(),
            'province': kwargs.get('province') or faker.province(),
            'city': kwargs.get('city') or faker.city(),
            'district': kwargs.get('district') or faker.district(),
            'detailAddress': kwargs.get('detail_address') or faker.street_address(),
            'isDefault': kwargs.get('is_default', False),
        }

    def generate_merchant(self, **kwargs) -> Dict[str, Any]:
        """Generate merchant data"""
        name = kwargs.get('name') or f"{self.merchant_prefix}{faker.company()}"
        return {
            'name': name,
            'categoryId': kwargs.get('category_id', random.randint(1, 10)),
            'phone': kwargs.get('phone') or self.generate_phone(),
            'address': kwargs.get('address') or faker.address(),
            'description': kwargs.get('description') or faker.text(max_nb_chars=200),
            'logo': kwargs.get('logo') or faker.image_url(),
            'businessHours': kwargs.get('business_hours') or "09:00-22:00",
            'minOrderAmount': kwargs.get('min_order_amount', round(random.uniform(10, 50), 2)),
            'deliveryFee': kwargs.get('delivery_fee', round(random.uniform(0, 10), 2)),
            'status': kwargs.get('status', 1),  # 1: active
        }

    def generate_product(self, merchant_id: int, **kwargs) -> Dict[str, Any]:
        """Generate product data"""
        name = kwargs.get('name') or f"{self.product_prefix}{faker.word()}"
        return {
            'merchantId': merchant_id,
            'categoryId': kwargs.get('category_id', random.randint(1, 20)),
            'name': name,
            'description': kwargs.get('description') or faker.text(max_nb_chars=100),
            'price': kwargs.get('price', round(random.uniform(5, 100), 2)),
            'originalPrice': kwargs.get('original_price', round(random.uniform(10, 150), 2)),
            'stock': kwargs.get('stock', random.randint(10, 1000)),
            'unit': kwargs.get('unit', '份'),
            'image': kwargs.get('image') or faker.image_url(),
            'status': kwargs.get('status', 1),  # 1: on sale
        }

    def generate_order_item(self, product_id: int, **kwargs) -> Dict[str, Any]:
        """Generate order item data"""
        quantity = kwargs.get('quantity', random.randint(1, 5))
        price = kwargs.get('price', round(random.uniform(5, 100), 2))
        return {
            'productId': product_id,
            'productName': kwargs.get('product_name') or faker.word(),
            'productImage': kwargs.get('product_image') or faker.image_url(),
            'quantity': quantity,
            'price': price,
            'totalAmount': round(quantity * price, 2),
        }

    def generate_order(self, user_id: int, merchant_id: int, **kwargs) -> Dict[str, Any]:
        """Generate order data"""
        items = kwargs.get('items', [self.generate_order_item(random.randint(1, 100)) for _ in range(random.randint(1, 3))])
        total_amount = sum(item['totalAmount'] for item in items)

        return {
            'userId': user_id,
            'merchantId': merchant_id,
            'items': items,
            'totalAmount': total_amount,
            'deliveryFee': kwargs.get('delivery_fee', round(random.uniform(0, 10), 2)),
            'discountAmount': kwargs.get('discount_amount', 0),
            'payAmount': total_amount + kwargs.get('delivery_fee', 0) - kwargs.get('discount_amount', 0),
            'remark': kwargs.get('remark') or faker.text(max_nb_chars=100),
            'deliveryType': kwargs.get('delivery_type', 1),  # 1: delivery, 2: pickup
            'addressId': kwargs.get('address_id'),
            'pickupCode': kwargs.get('pickup_code'),
        }

    def generate_review(self, user_id: int, order_id: int, merchant_id: int, **kwargs) -> Dict[str, Any]:
        """Generate review data"""
        return {
            'userId': user_id,
            'orderId': order_id,
            'merchantId': merchant_id,
            'rating': kwargs.get('rating', random.randint(3, 5)),
            'content': kwargs.get('content') or faker.text(max_nb_chars=200),
            'images': kwargs.get('images', [faker.image_url() for _ in range(random.randint(0, 3))]),
            'isAnonymous': kwargs.get('is_anonymous', random.choice([True, False])),
        }

    def generate_coupon(self, **kwargs) -> Dict[str, Any]:
        """Generate coupon data"""
        now = datetime.now()
        return {
            'name': kwargs.get('name') or f"优惠券{faker.word()}",
            'type': kwargs.get('type', random.choice([1, 2])),  # 1: amount, 2: discount
            'value': kwargs.get('value', round(random.uniform(5, 50), 2)),
            'minOrderAmount': kwargs.get('min_order_amount', round(random.uniform(20, 100), 2)),
            'startTime': kwargs.get('start_time') or now.strftime('%Y-%m-%d %H:%M:%S'),
            'endTime': kwargs.get('end_time') or (now + timedelta(days=30)).strftime('%Y-%m-%d %H:%M:%S'),
            'totalCount': kwargs.get('total_count', random.randint(100, 1000)),
            'limitPerUser': kwargs.get('limit_per_user', 1),
        }


# Global data generator instance
_data_generator: Optional[DataGenerator] = None


def get_data_generator() -> DataGenerator:
    """Get data generator instance"""
    global _data_generator
    if _data_generator is None:
        _data_generator = DataGenerator()
    return _data_generator
