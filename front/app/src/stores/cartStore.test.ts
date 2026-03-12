import { describe, it, expect, beforeEach } from 'vitest'
import { useCartStore, type CartItem } from '@/stores/cartStore'
import { act } from '@testing-library/react'

describe('useCartStore', () => {
  beforeEach(() => {
    useCartStore.setState({ cart: { items: [] } })
  })

  const mockDish = {
    id: 1,
    storeId: 1,
    name: 'Test Dish',
    description: 'A test dish',
    price: 25.00,
    image: 'test.jpg',
    category: 'main',
    isRecommended: false,
    isSpicy: false,
    sales: 100,
    originalPrice: 30,
    tags: [],
    isAvailable: true,
  }

  const mockCartItem: CartItem = {
    productId: 1,
    merchantId: 1,
    merchantName: 'Test Store',
    name: 'Test Item',
    price: 25.00,
    quantity: 1,
    image: 'test.jpg',
  }

  describe('addItem', () => {
    it('should add a new item to cart', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].name).toBe('Test Item')
    })

    it('should increase quantity when adding existing item', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(mockCartItem)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].quantity).toBe(2)
    })

    it('should add different items separately', () => {
      const item2 = { ...mockCartItem, productId: 2, name: 'Item 2' }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(2)
    })
  })

  describe('addToCart', () => {
    it('should add dish to cart with correct properties', () => {
      act(() => {
        useCartStore.getState().addToCart(mockDish, 1, 'Test Store')
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].productId).toBe(1)
      expect(cart.items[0].merchantId).toBe(1)
      expect(cart.items[0].merchantName).toBe('Test Store')
      expect(cart.items[0].price).toBe(25.00)
      expect(cart.items[0].quantity).toBe(1)
    })

    it('should increment quantity when adding same dish', () => {
      act(() => {
        useCartStore.getState().addToCart(mockDish, 1, 'Test Store')
        useCartStore.getState().addToCart(mockDish, 1, 'Test Store')
        useCartStore.getState().addToCart(mockDish, 1, 'Test Store')
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].quantity).toBe(3)
    })
  })

  describe('removeFromCart / removeItem', () => {
    it('should remove item from cart by dishId', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(1)
      
      act(() => {
        useCartStore.getState().removeFromCart(1)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(0)
    })

    it('should remove item from cart by productId', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(1)
      
      act(() => {
        useCartStore.getState().removeItem(1)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(0)
    })

    it('should not affect other items when removing', () => {
      const item2 = { ...mockCartItem, productId: 2, name: 'Item 2' }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(2)
      
      act(() => {
        useCartStore.getState().removeItem(1)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].productId).toBe(2)
    })
  })

  describe('updateQuantity', () => {
    it('should update item quantity', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().updateQuantity(1, 5)
      })
      expect(useCartStore.getState().cart.items[0].quantity).toBe(5)
    })

    it('should remove item when quantity is 0 or less', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().updateQuantity(1, 0)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(0)
    })

    it('should remove item when quantity is negative', () => {
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().updateQuantity(1, -1)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(0)
    })
  })

  describe('clearCart', () => {
    it('should clear all items from cart', () => {
      const item2 = { ...mockCartItem, productId: 2 }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(2)
      
      act(() => {
        useCartStore.getState().clearCart()
      })
      expect(useCartStore.getState().cart.items).toHaveLength(0)
    })
  })

  describe('clearMerchantItems', () => {
    it('should clear items from specific merchant', () => {
      const item2 = { ...mockCartItem, productId: 2, merchantId: 2 }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().cart.items).toHaveLength(2)
      
      act(() => {
        useCartStore.getState().clearMerchantItems(1)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items).toHaveLength(1)
      expect(cart.items[0].merchantId).toBe(2)
    })
  })

  describe('getTotalCount', () => {
    it('should return 0 for empty cart', () => {
      expect(useCartStore.getState().getTotalCount()).toBe(0)
    })

    it('should return total quantity of all items', () => {
      const item2 = { ...mockCartItem, productId: 2 }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().getTotalCount()).toBe(3)
    })
  })

  describe('getTotalPrice', () => {
    it('should return 0 for empty cart', () => {
      expect(useCartStore.getState().getTotalPrice()).toBe(0)
    })

    it('should calculate total price correctly', () => {
      const item2 = { ...mockCartItem, productId: 2, price: 15 }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().getTotalPrice()).toBe(65)
    })
  })

  describe('getMerchantTotal', () => {
    it('should return 0 for merchant with no items', () => {
      expect(useCartStore.getState().getMerchantTotal(1)).toBe(0)
    })

    it('should calculate total for specific merchant', () => {
      const item2 = { ...mockCartItem, productId: 2, merchantId: 2, price: 30 }
      act(() => {
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(mockCartItem)
        useCartStore.getState().addItem(item2)
      })
      expect(useCartStore.getState().getMerchantTotal(1)).toBe(50)
      expect(useCartStore.getState().getMerchantTotal(2)).toBe(30)
    })
  })
})
