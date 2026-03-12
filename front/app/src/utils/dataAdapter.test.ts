import { describe, it, expect } from 'vitest'
import {
  adaptMerchant,
  adaptMerchantList,
  adaptProduct,
  adaptProductList,
  adaptOrder,
  adaptOrderList,
  adaptOrderStatus,
  convertOrderStatusToBackend,
  adaptUser,
  adaptPaginatedResponse,
} from '@/utils/dataAdapter'
import { OrderStatus } from '@/types'

describe('adaptMerchant', () => {
  it('should adapt backend merchant data to Store format', () => {
    const backendData = {
      id: 1,
      name: 'Test Store',
      logo: 'logo.jpg',
      rating: 4.8,
      salesVolume: 1000,
      deliveryTime: 30,
      minPrice: 20,
    }
    const result = adaptMerchant(backendData)
    expect(result.id).toBe(1)
    expect(result.name).toBe('Test Store')
    expect(result.rating).toBe(4.8)
    expect(result.monthlySales).toBe(1000)
  })

  it('should handle nested merchant structure', () => {
    const backendData = {
      merchant: {
        id: 2,
        name: 'Nested Store',
      },
      category: { id: 1, name: 'Food' },
    }
    const result = adaptMerchant(backendData)
    expect(result.id).toBe(2)
    expect(result.name).toBe('Nested Store')
  })

  it('should use default values for missing fields', () => {
    const backendData = { id: 1, name: 'Store' }
    const result = adaptMerchant(backendData)
    expect(result.rating).toBe(4.5)
    expect(result.monthlySales).toBe(0)
    expect(result.deliveryTime).toBe(30)
  })

  it('should return null for null input', () => {
    const result = adaptMerchant(null)
    expect(result).toBeNull()
  })
})

describe('adaptMerchantList', () => {
  it('should adapt array of merchants', () => {
    const backendList = [
      { id: 1, name: 'Store 1' },
      { id: 2, name: 'Store 2' },
    ]
    const result = adaptMerchantList(backendList)
    expect(result).toHaveLength(2)
    expect(result[0].name).toBe('Store 1')
    expect(result[1].name).toBe('Store 2')
  })

  it('should return empty array for null input', () => {
    expect(adaptMerchantList(null as any)).toEqual([])
  })

  it('should return empty array for undefined input', () => {
    expect(adaptMerchantList(undefined as any)).toEqual([])
  })

  it('should return empty array for non-array input', () => {
    expect(adaptMerchantList({} as any)).toEqual([])
  })
})

describe('adaptProduct', () => {
  it('should adapt backend product data to Dish format', () => {
    const backendData = {
      id: 1,
      merchantId: 10,
      name: 'Test Dish',
      description: 'Delicious dish',
      price: 28.00,
      image: 'dish.jpg',
      categoryId: 1,
      isRecommended: true,
      isSpicy: true,
      salesVolume: 500,
      status: 1,
    }
    const result = adaptProduct(backendData)
    expect(result.id).toBe(1)
    expect(result.storeId).toBe(10)
    expect(result.name).toBe('Test Dish')
    expect(result.price).toBe(28)
    expect(result.isRecommended).toBe(true)
    expect(result.isSpicy).toBe(true)
    expect(result.isAvailable).toBe(true)
  })

  it('should use default image when not provided', () => {
    const backendData = { id: 1, name: 'Dish', price: 20 }
    const result = adaptProduct(backendData)
    expect(result.image).toBeTruthy()
  })

  it('should handle status field correctly', () => {
    const availableProduct = adaptProduct({ id: 1, status: 1 })
    const unavailableProduct = adaptProduct({ id: 2, status: 0 })
    expect(availableProduct.isAvailable).toBe(true)
    expect(unavailableProduct.isAvailable).toBe(false)
  })

  it('should return null for null input', () => {
    const result = adaptProduct(null)
    expect(result).toBeNull()
  })
})

describe('adaptProductList', () => {
  it('should adapt array of products', () => {
    const backendList = [
      { id: 1, name: 'Dish 1', price: 10 },
      { id: 2, name: 'Dish 2', price: 20 },
    ]
    const result = adaptProductList(backendList)
    expect(result).toHaveLength(2)
    expect(result[0].name).toBe('Dish 1')
    expect(result[1].price).toBe(20)
  })

  it('should return empty array for non-array input', () => {
    expect(adaptProductList(null as any)).toEqual([])
    expect(adaptProductList(undefined as any)).toEqual([])
  })
})

describe('adaptOrderStatus', () => {
  it('should pass through order status', () => {
    expect(adaptOrderStatus(1)).toBe(OrderStatus.PENDING_PAYMENT)
    expect(adaptOrderStatus(2)).toBe(OrderStatus.PENDING_ACCEPTANCE)
    expect(adaptOrderStatus(3)).toBe(OrderStatus.PREPARING)
  })
})

describe('convertOrderStatusToBackend', () => {
  it('should convert frontend status to backend code', () => {
    expect(convertOrderStatusToBackend(OrderStatus.PENDING_PAYMENT)).toBe(1)
    expect(convertOrderStatusToBackend(OrderStatus.PENDING_ACCEPTANCE)).toBe(2)
  })
})

describe('adaptOrder', () => {
  it('should adapt backend order data to Order format', () => {
    const backendData = {
      id: 1,
      orderNo: 'ORD123',
      merchantId: 10,
      merchantName: 'Test Store',
      userId: 100,
      status: 1,
      payStatus: 1,
      totalAmount: 100.00,
      actualAmount: 90.00,
      remark: 'No spicy',
      items: [
        { productId: 1, name: 'Dish 1', price: 50, quantity: 2 },
      ],
      address: {
        contactName: 'John',
        contactPhone: '13800138000',
        detail: '123 Main St',
      },
      createTime: '2024-01-15T10:00:00',
    }
    const result = adaptOrder(backendData)
    expect(result.id).toBe(1)
    expect(result.orderNo).toBe('ORD123')
    expect(result.merchantName).toBe('Test Store')
    expect(result.totalAmount).toBe(100)
    expect(result.actualAmount).toBe(90)
    expect(result.items).toHaveLength(1)
    expect(result.items[0].name).toBe('Dish 1')
    expect(result.address?.contactName).toBe('John')
  })

  it('should handle missing optional fields', () => {
    const backendData = {
      id: 1,
      orderNo: 'ORD123',
      merchantId: 10,
      userId: 100,
      status: 0,
      totalAmount: 50,
    }
    const result = adaptOrder(backendData)
    expect(result.merchantName).toBe('')
    expect(result.remark).toBe('')
    expect(result.items).toEqual([])
    expect(result.address).toBeUndefined()
  })

  it('should return null for null input', () => {
    const result = adaptOrder(null)
    expect(result).toBeNull()
  })
})

describe('adaptOrderList', () => {
  it('should adapt array of orders', () => {
    const backendList = [
      { id: 1, orderNo: 'ORD1', totalAmount: 100 },
      { id: 2, orderNo: 'ORD2', totalAmount: 200 },
    ]
    const result = adaptOrderList(backendList)
    expect(result).toHaveLength(2)
    expect(result[0].orderNo).toBe('ORD1')
    expect(result[1].totalAmount).toBe(200)
  })

  it('should return empty array for non-array input', () => {
    expect(adaptOrderList(null as any)).toEqual([])
    expect(adaptOrderList(undefined as any)).toEqual([])
  })
})

describe('adaptUser', () => {
  it('should adapt backend user data to User format', () => {
    const backendData = {
      id: 1,
      phone: '13800138000',
      nickname: 'TestUser',
      avatar: 'avatar.jpg',
      gender: 1,
      userType: 'user',
      status: 1,
    }
    const result = adaptUser(backendData)
    expect(result.id).toBe(1)
    expect(result.phone).toBe('13800138000')
    expect(result.nickname).toBe('TestUser')
    expect(result.avatar).toBe('avatar.jpg')
  })

  it('should generate default nickname from phone', () => {
    const backendData = {
      id: 1,
      phone: '13800138000',
    }
    const result = adaptUser(backendData)
    expect(result.nickname).toContain('用户')
  })

  it('should generate default avatar using dicebear', () => {
    const backendData = { id: 123 }
    const result = adaptUser(backendData)
    expect(result.avatar).toContain('dicebear')
    expect(result.avatar).toContain('123')
  })

  it('should return null for null input', () => {
    const result = adaptUser(null)
    expect(result).toBeNull()
  })
})

describe('adaptPaginatedResponse', () => {
  const itemAdapter = (item: any) => ({ id: item.id, name: item.name })

  it('should adapt paginated response with list field', () => {
    const backendData = {
      list: [{ id: 1, name: 'Item 1' }, { id: 2, name: 'Item 2' }],
      total: 100,
      page: 1,
      pageSize: 10,
    }
    const result = adaptPaginatedResponse(backendData, itemAdapter)
    expect(result.list).toHaveLength(2)
    expect(result.total).toBe(100)
    expect(result.page).toBe(1)
    expect(result.pageSize).toBe(10)
  })

  it('should adapt paginated response with merchantList field', () => {
    const backendData = {
      merchantList: [{ id: 1, name: 'Store 1' }],
      total: 50,
    }
    const result = adaptPaginatedResponse(backendData, itemAdapter)
    expect(result.list).toHaveLength(1)
    expect(result.total).toBe(50)
  })

  it('should adapt paginated response with productList field', () => {
    const backendData = {
      productList: [{ id: 1, name: 'Product 1' }],
      total: 25,
    }
    const result = adaptPaginatedResponse(backendData, itemAdapter)
    expect(result.list).toHaveLength(1)
  })

  it('should return default values for null input', () => {
    const result = adaptPaginatedResponse(null, itemAdapter)
    expect(result.list).toEqual([])
    expect(result.total).toBe(0)
    expect(result.page).toBe(1)
    expect(result.pageSize).toBe(10)
  })
})
