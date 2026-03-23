import requests
import json

base_url = 'http://localhost:9090'

# 测试公共接口
endpoints = [
    ('GET', '/api/merchant/list'),
    ('GET', '/api/merchant/1'),
    ('GET', '/api/product/list'),
    ('GET', '/api/product/1'),
    ('GET', '/api/product/category/list'),
    ('GET', '/api/coupon/list'),
    ('GET', '/actuator/health'),
]

print("Testing public endpoints:")
print("=" * 50)

for method, endpoint in endpoints:
    try:
        url = f'{base_url}{endpoint}'
        if method == 'GET':
            resp = requests.get(url, timeout=5)
        status = "OK" if resp.status_code == 200 else "FAIL"
        print(f'{method} {endpoint}: {resp.status_code} [{status}]')
    except Exception as e:
        print(f'{method} {endpoint}: ERROR - {str(e)[:50]}')

# 测试登录
print("\nTesting auth endpoints:")
print("=" * 50)

login_data = {'phone': '13800000011', 'password': '123456'}
try:
    resp = requests.post(f'{base_url}/api/auth/login', json=login_data, timeout=5)
    print(f'POST /api/auth/login: {resp.status_code}')
    if resp.status_code == 200:
        print('  Login successful!')
    else:
        data = resp.json()
        print(f'  Message: {data.get("message")}')
except Exception as e:
    print(f'POST /api/auth/login: ERROR - {e}')
