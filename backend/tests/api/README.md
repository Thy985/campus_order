# 校园点餐管理系统 - API接口自动化测试框架

## 项目概述

本项目是一个基于Python的API接口自动化测试框架，用于对校园点餐管理系统的后端API进行全面测试。框架支持功能验证、边界条件测试、错误处理测试、性能测试和安全性测试。

## 项目结构

```
backend/tests/api/
├── config/                 # 配置文件目录
│   ├── dev.yaml           # 开发环境配置
│   ├── test.yaml          # 测试环境配置
│   ├── prod.yaml          # 生产环境配置
│   └── settings.py        # 配置管理模块
├── test_cases/            # 测试用例目录
│   ├── auth/              # 认证相关测试
│   ├── user/              # 用户管理测试
│   ├── merchant/          # 商家相关测试
│   ├── product/           # 商品相关测试
│   ├── order/             # 订单相关测试
│   ├── payment/           # 支付相关测试
│   └── review/            # 评价相关测试
├── utils/                 # 工具类目录
│   ├── http_client.py     # HTTP客户端封装
│   ├── data_generator.py  # 测试数据生成器
│   ├── assertions.py      # 自定义断言
│   ├── validators.py      # 响应验证器
│   └── logger.py          # 日志配置
├── fixtures/              # 测试夹具目录
├── reports/               # 测试报告输出目录
├── conftest.py            # Pytest配置文件
├── pytest.ini             # Pytest配置
├── requirements.txt       # 依赖包列表
└── README.md              # 项目说明文档
```

## 功能特性

### 1. 测试框架特性
- 基于 **pytest** 的测试框架
- 支持多环境配置（开发、测试、生产）
- 自动化的测试数据生成和管理
- 详细的请求/响应日志记录
- HTML和JSON格式的测试报告
- 支持并行执行（pytest-xdist）

### 2. HTTP客户端特性
- 基于 **requests** 库的HTTP客户端封装
- 自动重试机制（指数退避）
- 超时控制
- 认证令牌管理
- 请求/响应日志记录

### 3. 测试数据管理
- 使用 **Faker** 生成真实测试数据
- 支持用户、商家、商品、订单、评价等数据生成
- 测试数据自动清理

### 4. 断言和验证
- 丰富的自定义断言方法
- JSON Schema验证
- 字段级验证器
- 敏感信息泄露检查

### 5. 测试覆盖范围
- **认证API**: 注册、登录、验证码、登出
- **用户管理API**: 用户信息、密码修改、地址管理
- **商家API**: 商家列表、详情、分类、搜索
- **商品API**: 商品列表、详情、分类、搜索
- **订单API**: 创建、查询、取消、状态流转
- **支付API**: 创建、查询、回调、关闭
- **评价API**: 创建、查询、商家回复

## 安装依赖

```bash
# 进入项目目录
cd backend/tests/api

# 创建虚拟环境（推荐）
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

## 使用方法

### 1. 配置测试环境

编辑 `config/test.yaml` 文件，配置API基础URL和其他参数：

```yaml
api:
  base_url: http://localhost:8080
  timeout: 30
```

### 2. 运行所有测试

```bash
# 运行所有测试
pytest

# 运行特定模块的测试
pytest test_cases/auth/
pytest test_cases/user/
pytest test_cases/merchant/

# 运行带有特定标记的测试
pytest -m smoke
pytest -m auth
pytest -m order
```

### 3. 生成测试报告

```bash
# 生成HTML报告
pytest --html=reports/report.html --self-contained-html

# 生成JSON报告
pytest --json-report --json-report-file=reports/report.json
```

### 4. 并行执行

```bash
# 使用4个进程并行执行
pytest -n 4

# 自动检测CPU核心数
pytest -n auto
```

### 5. 指定环境

```bash
# 使用开发环境
set ENV=dev
pytest

# 使用测试环境
set ENV=test
pytest

# 使用生产环境（谨慎使用）
set ENV=prod
pytest
```

## 测试用例说明

### 认证测试 (test_cases/auth/test_auth.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestAuthRegister | test_register_success | 正常注册 |
| TestAuthRegister | test_register_duplicate_phone | 重复手机号注册 |
| TestAuthRegister | test_register_invalid_phone | 无效手机号注册 |
| TestAuthRegister | test_register_weak_password | 弱密码注册 |
| TestAuthLogin | test_login_success | 正常登录 |
| TestAuthLogin | test_login_wrong_password | 错误密码登录 |
| TestAuthLogin | test_login_nonexistent_user | 不存在的用户登录 |
| TestAuthVerifyCode | test_send_verify_code_success | 发送验证码 |
| TestAuthLogout | test_logout_success | 正常登出 |

### 用户管理测试 (test_cases/user/test_user.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestUserInfo | test_get_user_info_success | 获取用户信息 |
| TestUserInfo | test_update_user_info_success | 更新用户信息 |
| TestUserPassword | test_change_password_success | 修改密码 |
| TestUserAddress | test_add_address_success | 添加地址 |
| TestUserAddress | test_get_address_list | 获取地址列表 |
| TestUserAddress | test_update_address | 更新地址 |
| TestUserAddress | test_delete_address | 删除地址 |

### 商家测试 (test_cases/merchant/test_merchant.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestMerchantList | test_get_merchant_list_success | 获取商家列表 |
| TestMerchantList | test_get_merchant_list_with_pagination | 分页查询 |
| TestMerchantDetail | test_get_merchant_detail_success | 获取商家详情 |
| TestMerchantCategory | test_get_merchant_categories | 获取商家分类 |
| TestMerchantSearch | test_search_merchant_by_name | 按名称搜索 |
| TestMerchantProducts | test_get_merchant_products | 获取商家商品 |
| TestMerchantReviews | test_get_merchant_reviews | 获取商家评价 |

### 商品测试 (test_cases/product/test_product.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestProductList | test_get_product_list_success | 获取商品列表 |
| TestProductList | test_get_product_list_with_pagination | 分页查询 |
| TestProductDetail | test_get_product_detail_success | 获取商品详情 |
| TestProductCategory | test_get_product_categories | 获取商品分类 |
| TestProductSearch | test_search_product_by_name | 按名称搜索 |
| TestProductStock | test_check_product_stock | 检查库存 |

### 订单测试 (test_cases/order/test_order.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestOrderCreate | test_create_order_success | 创建订单 |
| TestOrderCreate | test_create_order_insufficient_stock | 库存不足 |
| TestOrderQuery | test_get_order_list | 获取订单列表 |
| TestOrderQuery | test_get_order_detail | 获取订单详情 |
| TestOrderCancel | test_cancel_order_success | 取消订单 |

### 支付测试 (test_cases/payment/test_payment.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestPaymentCreate | test_create_payment_success | 创建支付 |
| TestPaymentQuery | test_get_payment_list | 获取支付列表 |
| TestPaymentCallback | test_wechat_pay_callback | 微信支付回调 |
| TestPaymentCallback | test_alipay_callback | 支付宝回调 |
| TestPaymentClose | test_close_payment | 关闭支付 |

### 评价测试 (test_cases/review/test_review.py)

| 测试类 | 测试方法 | 说明 |
|--------|----------|------|
| TestReviewCreate | test_create_review_success | 创建评价 |
| TestReviewQuery | test_get_review_list | 获取评价列表 |
| TestMerchantReply | test_reply_to_review | 商家回复 |
| TestReviewLike | test_like_review | 点赞评价 |

## 测试标记说明

- `@pytest.mark.smoke`: 冒烟测试（快速验证核心功能）
- `@pytest.mark.auth`: 认证相关测试
- `@pytest.mark.user`: 用户管理测试
- `@pytest.mark.merchant`: 商家相关测试
- `@pytest.mark.product`: 商品相关测试
- `@pytest.mark.order`: 订单相关测试
- `@pytest.mark.payment`: 支付相关测试
- `@pytest.mark.review`: 评价相关测试
- `@pytest.mark.boundary`: 边界条件测试
- `@pytest.mark.error`: 错误处理测试
- `@pytest.mark.performance`: 性能测试
- `@pytest.mark.security`: 安全性测试
- `@pytest.mark.slow`: 慢速测试

## 自定义断言

框架提供了丰富的自定义断言方法：

```python
from utils.assertions import (
    assert_status_code,      # 验证状态码
    assert_success,          # 验证成功响应
    assert_response_time,    # 验证响应时间
    assert_has_field,        # 验证字段存在
    assert_field_equals,     # 验证字段值
    assert_has_pagination,   # 验证分页字段
    assert_no_sensitive_data # 验证无敏感数据
)

# 使用示例
def test_example():
    response = http_client.get("/api/user/info")
    assert_success(response)
    assert_has_field(response, "data")
    assert_response_time(response, 2.0)  # 响应时间小于2秒
```

## 测试数据生成

使用DataGenerator生成测试数据：

```python
from utils.data_generator import get_data_generator

data_generator = get_data_generator()

# 生成用户数据
user_data = data_generator.generate_user()

# 生成商家数据
merchant_data = data_generator.generate_merchant()

# 生成商品数据
product_data = data_generator.generate_product(merchant_id=1)

# 生成订单数据
order_data = data_generator.generate_order(user_id=1, merchant_id=1)

# 生成评价数据
review_data = data_generator.generate_review(user_id=1, order_id=1, merchant_id=1)
```

## 配置说明

### 环境变量

- `ENV`: 指定测试环境（dev/test/prod）
- `API_BASE_URL`: API基础URL
- `DB_PASSWORD`: 数据库密码（生产环境）

### 配置文件

配置文件位于 `config/` 目录，支持YAML格式：

```yaml
# 测试环境配置示例
env: test
api:
  base_url: http://localhost:8080
  timeout: 30
  retry:
    max_attempts: 3
    backoff_factor: 1

database:
  host: localhost
  port: 3306
  name: campus_order_test
  user: root
  password: "123456"

test_data:
  user:
    prefix: test_user_
    password: Test@123456
```

## 测试报告

测试执行后会生成以下报告：

1. **HTML报告**: `reports/report.html`
   - 测试摘要
   - 详细的测试用例结果
   - 失败用例的详细信息

2. **JSON报告**: `reports/report.json`
   - 结构化的测试结果
   - 便于CI/CD集成

3. **日志文件**: `reports/test.log`
   - 详细的请求/响应日志
   - 调试信息

## 最佳实践

1. **测试隔离**: 每个测试用例应该独立，不依赖其他测试的执行顺序
2. **数据清理**: 使用fixture的teardown进行数据清理
3. **错误处理**: 测试应该验证错误场景，而不仅仅是成功场景
4. **性能考虑**: 使用pytest-xdist进行并行执行，提高测试效率
5. **环境分离**: 使用不同的配置文件分离不同环境的配置

## 常见问题

### 1. 测试执行失败，提示连接错误

确保后端服务已启动，并检查 `config/test.yaml` 中的 `base_url` 配置是否正确。

### 2. 认证测试失败

检查验证码配置，测试环境通常使用固定的测试验证码（如123456）。

### 3. 并行执行时测试失败

确保测试用例之间没有数据冲突，使用不同的测试数据。

## 贡献指南

1. 添加新的测试用例时，请遵循现有的代码风格
2. 为新的API添加对应的测试标记
3. 更新README.md文档
4. 确保所有测试通过后再提交

## 许可证

MIT License
