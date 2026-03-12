import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { OrderCard } from '@/components/common/OrderCard'
import { OrderStatus, OrderStatusText } from '@/types'
import type { Order } from '@/types'

const mockOrder: Order = {
  id: 1,
  orderNo: 'ORD123456',
  merchantId: 1,
  merchantName: 'Test Store',
  merchantLogo: 'logo.jpg',
  userId: 1,
  status: OrderStatus.PENDING_ACCEPTANCE,
  payStatus: 1,
  totalAmount: 100,
  actualAmount: 90,
  remark: 'No spicy please',
  items: [
    { productId: 1, name: 'Dish 1', price: 50, quantity: 2, image: 'dish1.jpg' },
    { productId: 2, name: 'Dish 2', price: 25, quantity: 1, image: 'dish2.jpg' },
  ],
  createTime: '2024-01-15 10:30:00',
}

const renderOrderCard = (order: Order, props = {}) => {
  return render(<OrderCard order={order} {...props} />)
}

describe('OrderCard', () => {
  it('should render order information correctly', () => {
    renderOrderCard(mockOrder)
    
    expect(screen.getByText('Test Store')).toBeInTheDocument()
    expect(screen.getByText('2024-01-15 10:30:00')).toBeInTheDocument()
    expect(screen.getByText('¥90')).toBeInTheDocument()
    expect(screen.getByText('共3件')).toBeInTheDocument()
  })

  it('should render order items', () => {
    renderOrderCard(mockOrder)
    
    expect(screen.getByText('Dish 1')).toBeInTheDocument()
    expect(screen.getByText('Dish 2')).toBeInTheDocument()
    expect(screen.getByText('x2')).toBeInTheDocument()
    expect(screen.getByText('x1')).toBeInTheDocument()
  })

  it('should show correct status badge for PENDING_PAYMENT', () => {
    const order = { ...mockOrder, status: OrderStatus.PENDING_PAYMENT }
    renderOrderCard(order)
    
    expect(screen.getByText(OrderStatusText[OrderStatus.PENDING_PAYMENT])).toBeInTheDocument()
    expect(screen.getByText('去支付')).toBeInTheDocument()
  })

  it('should show correct status badge for READY_FOR_PICKUP', () => {
    const order = { ...mockOrder, status: OrderStatus.READY_FOR_PICKUP }
    renderOrderCard(order)
    
    expect(screen.getByText(OrderStatusText[OrderStatus.READY_FOR_PICKUP])).toBeInTheDocument()
    expect(screen.getByText('确认取餐')).toBeInTheDocument()
  })

  it('should show correct status badge for COMPLETED', () => {
    const order = { ...mockOrder, status: OrderStatus.COMPLETED }
    renderOrderCard(order)
    
    expect(screen.getByText(OrderStatusText[OrderStatus.COMPLETED])).toBeInTheDocument()
    expect(screen.getByText('再来一单')).toBeInTheDocument()
  })

  it('should show correct status badge for CANCELLED', () => {
    const order = { ...mockOrder, status: OrderStatus.CANCELLED }
    renderOrderCard(order)
    
    expect(screen.getByText(OrderStatusText[OrderStatus.CANCELLED])).toBeInTheDocument()
  })

  it('should call onPay when pay button is clicked', () => {
    const order = { ...mockOrder, status: OrderStatus.PENDING_PAYMENT }
    const onPay = vi.fn()
    renderOrderCard(order, { onPay })
    
    fireEvent.click(screen.getByText('去支付'))
    expect(onPay).toHaveBeenCalledWith(1)
  })

  it('should call onConfirmPickup when confirm button is clicked', () => {
    const order = { ...mockOrder, status: OrderStatus.READY_FOR_PICKUP }
    const onConfirmPickup = vi.fn()
    renderOrderCard(order, { onConfirmPickup })
    
    fireEvent.click(screen.getByText('确认取餐'))
    expect(onConfirmPickup).toHaveBeenCalledWith(1)
  })

  it('should call onReorder when reorder button is clicked', () => {
    const order = { ...mockOrder, status: OrderStatus.COMPLETED }
    const onReorder = vi.fn()
    renderOrderCard(order, { onReorder })
    
    fireEvent.click(screen.getByText('再来一单'))
    expect(onReorder).toHaveBeenCalledWith(order)
  })

  it('should expand and show order details', () => {
    renderOrderCard(mockOrder)
    
    const expandButtons = screen.getAllByRole('button')
    const expandButton = expandButtons.find(btn => btn.querySelector('svg.lucide-chevron-down'))
    
    if (expandButton) {
      fireEvent.click(expandButton)
      
      expect(screen.getByText('订单编号')).toBeInTheDocument()
      expect(screen.getByText('ORD123456')).toBeInTheDocument()
      expect(screen.getByText('商品总额')).toBeInTheDocument()
      expect(screen.getByText('¥100')).toBeInTheDocument()
      expect(screen.getByText('备注')).toBeInTheDocument()
      expect(screen.getByText('No spicy please')).toBeInTheDocument()
    }
  })

  it('should not show remark section when remark is empty', () => {
    const order = { ...mockOrder, remark: '' }
    renderOrderCard(order)
    
    const expandButtons = screen.getAllByRole('button')
    const expandButton = expandButtons.find(btn => btn.querySelector('svg.lucide-chevron-down'))
    
    if (expandButton) {
      fireEvent.click(expandButton)
      expect(screen.queryByText('备注')).not.toBeInTheDocument()
    }
  })

  it('should apply animation delay based on index', () => {
    const { container } = renderOrderCard(mockOrder, { index: 3 })
    
    const card = container.firstChild as HTMLElement
    expect(card).toHaveStyle({ animationDelay: '240ms' })
  })
})
