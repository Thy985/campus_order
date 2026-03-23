"""
Simple API test to verify backend connectivity and basic functionality
"""
import requests
import json

BASE_URL = "http://localhost:9090"

def test_health_check():
    """Test if backend is running"""
    try:
        response = requests.get(f"{BASE_URL}/api/auth/check", timeout=5)
        print(f"Health Check: Status {response.status_code}")
        print(f"Response: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"Health Check Failed: {e}")
        return False

def test_register():
    """Test registration with detailed output"""
    import random
    phone = f"138{random.randint(10000000, 99999999)}"
    
    data = {
        "nickname": "TestUser",
        "phone": phone,
        "password": "Test@123456",
        "verifyCode": "123456"
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/auth/register",
            json=data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        print(f"\nRegister Test:")
        print(f"  Status: {response.status_code}")
        print(f"  Request: {json.dumps(data, indent=2)}")
        print(f"  Response: {response.text}")
        return response.status_code == 200
    except Exception as e:
        print(f"Register Test Failed: {e}")
        return False

def test_login():
    """Test login with test account"""
    data = {
        "phone": "13800000011",  # Test student account from SQL
        "password": "123456"
    }
    
    try:
        response = requests.post(
            f"{BASE_URL}/api/auth/login",
            json=data,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        print(f"\nLogin Test:")
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.text[:200]}")
        return response.status_code == 200
    except Exception as e:
        print(f"Login Test Failed: {e}")
        return False

def test_get_merchants():
    """Test get merchant list"""
    try:
        response = requests.get(
            f"{BASE_URL}/api/merchant/list",
            params={"page": 1, "pageSize": 10},
            timeout=10
        )
        print(f"\nGet Merchants Test:")
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.text[:200]}")
        return response.status_code == 200
    except Exception as e:
        print(f"Get Merchants Test Failed: {e}")
        return False

def test_get_products():
    """Test get product list"""
    try:
        response = requests.get(
            f"{BASE_URL}/api/product/list",
            params={"page": 1, "pageSize": 10},
            timeout=10
        )
        print(f"\nGet Products Test:")
        print(f"  Status: {response.status_code}")
        print(f"  Response: {response.text[:200]}")
        return response.status_code == 200
    except Exception as e:
        print(f"Get Products Test Failed: {e}")
        return False

if __name__ == "__main__":
    print("=" * 60)
    print("Campus Order API - Simple Test Suite")
    print("=" * 60)
    
    results = []
    
    # Test 1: Health Check
    print("\n[1/5] Testing Health Check...")
    results.append(("Health Check", test_health_check()))
    
    # Test 2: Register
    print("\n[2/5] Testing Registration...")
    results.append(("Register", test_register()))
    
    # Test 3: Login
    print("\n[3/5] Testing Login...")
    results.append(("Login", test_login()))
    
    # Test 4: Get Merchants
    print("\n[4/5] Testing Get Merchants...")
    results.append(("Get Merchants", test_get_merchants()))
    
    # Test 5: Get Products
    print("\n[5/5] Testing Get Products...")
    results.append(("Get Products", test_get_products()))
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    passed = sum(1 for _, r in results if r)
    total = len(results)
    
    for name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"  {name}: {status}")
    
    print(f"\nTotal: {passed}/{total} tests passed ({passed/total*100:.1f}%)")
    print("=" * 60)
