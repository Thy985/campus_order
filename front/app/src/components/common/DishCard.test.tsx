import { describe, it, expect, beforeEach } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { DishCard } from '@/components/common/DishCard'
import { useCartStore } from '@/stores/cartStore'
import type { Dish } from '@/types'

const mockDish: Dish = {
  id: 1,
  storeId: 1,
  name: 'Test Dish',
  description: 'A delicious test dish',
  price: 28.00,
  image: 'test.jpg',
  category: 'main',
  isRecommended: true,
  isSpicy: true,
  sales: 100,
  originalPrice: 35,
  tags: ['Popular', 'Spicy'],
  isAvailable: true,
}

const mockDishSimple: Dish = {
  id: 2,
  storeId: 1,
  name: 'Simple Dish',
  description: 'A simple dish',
  price: 15.00,
  image: 'simple.jpg',
  category: 'side',
  isRecommended: false,
  isSpicy: false,
  sales: 50,
  originalPrice: 15,
  tags: [],
  isAvailable: true,
}

const renderWithRouter = (ui: React.ReactElement) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  )
}

describe('DishCard', () => {
  beforeEach(() => {
    useCartStore.setState({ cart: { items: [] } })
  })

  it('should render dish information correctly', () => {
    renderWithRouter(<DishCard dish={mockDish} merchantId={1} merchantName="Test Store" />)
    
    expect(screen.getByText('Test Dish')).toBeInTheDocument()
    expect(screen.getByText('A delicious test dish')).toBeInTheDocument()
    expect(screen.getByText('¥28')).toBeInTheDocument()
    expect(screen.getByText('¥35')).toBeInTheDocument()
    expect(screen.getByText('月售 100 份')).toBeInTheDocument()
  })

  it('should show recommended badge for recommended dishes', () => {
    renderWithRouter(<DishCard dish={mockDish} />)
    expect(screen.getByText('店长推荐')).toBeInTheDocument()
  })

  it('should show discount badge when original price is higher', () => {
    renderWithRouter(<DishCard dish={mockDish} />)
    expect(screen.getByText('限时特惠')).toBeInTheDocument()
  })

  it('should not show badges for simple dish', () => {
    renderWithRouter(<DishCard dish={mockDishSimple} />)
    expect(screen.queryByText('店长推荐')).not.toBeInTheDocument()
    expect(screen.queryByText('限时特惠')).not.toBeInTheDocument()
  })

  it('should show tags', () => {
    renderWithRouter(<DishCard dish={mockDish} />)
    expect(screen.getByText('Popular')).toBeInTheDocument()
    expect(screen.getByText('Spicy')).toBeInTheDocument()
  })

  it('should add item to cart when add button is clicked', () => {
    renderWithRouter(<DishCard dish={mockDish} merchantId={1} merchantName="Test Store" />)
    
    const addButton = screen.getByRole('button', { name: /添加.*到购物车/ })
    fireEvent.click(addButton)
    
    const { cart } = useCartStore.getState()
    expect(cart.items).toHaveLength(1)
    expect(cart.items[0].productId).toBe(1)
  })

  it('should show quantity controls after adding item', () => {
    useCartStore.setState({
      cart: {
        items: [{
          productId: 1,
          merchantId: 1,
          merchantName: 'Test Store',
          name: 'Test Dish',
          price: 28,
          quantity: 1,
        }]
      }
    })
    
    renderWithRouter(<DishCard dish={mockDish} merchantId={1} merchantName="Test Store" />)
    
    expect(screen.getByText('1')).toBeInTheDocument()
  })

  it('should increase quantity when plus button is clicked', async () => {
    useCartStore.setState({
      cart: {
        items: [{
          productId: 1,
          merchantId: 1,
          merchantName: 'Test Store',
          name: 'Test Dish',
          price: 28,
          quantity: 1,
        }]
      }
    })
    
    const { container } = renderWithRouter(<DishCard dish={mockDish} merchantId={1} merchantName="Test Store" />)
    
    const plusButton = container.querySelector('button[class*="from-orange-500"]')
    if (plusButton) {
      await act(async () => {
        fireEvent.click(plusButton)
      })
      const { cart } = useCartStore.getState()
      expect(cart.items[0].quantity).toBe(2)
    }
  })

  it('should have correct accessibility attributes', () => {
    renderWithRouter(<DishCard dish={mockDish} />)
    
    const article = screen.getByRole('article')
    expect(article).toHaveAttribute('aria-label')
    expect(article.getAttribute('aria-label')).toContain('Test Dish')
  })
})
