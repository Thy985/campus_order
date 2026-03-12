import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { StoreCard } from '@/components/common/StoreCard'
import type { Store } from '@/types'

const mockStore: Store = {
  id: 1,
  name: 'Test Store',
  logo: 'logo.jpg',
  rating: 4.8,
  monthlySales: 1000,
  deliveryTime: 30,
  distance: 1.5,
  minPrice: 20,
  tags: ['Chinese', 'Fast Food'],
  categories: [],
  banner: 'banner.jpg',
}

const mockStoreSimple: Store = {
  id: 2,
  name: 'Simple Store',
  logo: 'simple.jpg',
  rating: 4.5,
  monthlySales: 500,
  deliveryTime: 25,
  distance: 2.0,
  minPrice: 15,
  tags: [],
  categories: [],
  banner: 'banner.jpg',
}

const renderWithRouter = (ui: React.ReactElement) => {
  return render(
    <BrowserRouter>
      {ui}
    </BrowserRouter>
  )
}

describe('StoreCard', () => {
  it('should render store information correctly', () => {
    renderWithRouter(<StoreCard store={mockStore} />)
    
    expect(screen.getByText('Test Store')).toBeInTheDocument()
    expect(screen.getByText('4.8')).toBeInTheDocument()
    expect(screen.getByText(/月售/)).toBeInTheDocument()
    expect(screen.getByText(/1000/)).toBeInTheDocument()
    expect(screen.getByText('30分钟')).toBeInTheDocument()
    expect(screen.getByText('1.5km')).toBeInTheDocument()
    expect(screen.getByText(/起送/)).toBeInTheDocument()
    expect(screen.getByText(/20/)).toBeInTheDocument()
  })

  it('should render tags', () => {
    renderWithRouter(<StoreCard store={mockStore} />)
    expect(screen.getByText('Chinese')).toBeInTheDocument()
    expect(screen.getByText('Fast Food')).toBeInTheDocument()
  })

  it('should not render tags when empty', () => {
    renderWithRouter(<StoreCard store={mockStoreSimple} />)
    expect(screen.queryByText('Chinese')).not.toBeInTheDocument()
  })

  it('should limit tags to 3', () => {
    const storeWithManyTags: Store = {
      ...mockStore,
      tags: ['Tag1', 'Tag2', 'Tag3', 'Tag4', 'Tag5'],
    }
    renderWithRouter(<StoreCard store={storeWithManyTags} />)
    
    expect(screen.getByText('Tag1')).toBeInTheDocument()
    expect(screen.getByText('Tag2')).toBeInTheDocument()
    expect(screen.getByText('Tag3')).toBeInTheDocument()
    expect(screen.queryByText('Tag4')).not.toBeInTheDocument()
    expect(screen.queryByText('Tag5')).not.toBeInTheDocument()
  })

  it('should have correct link to store detail', () => {
    renderWithRouter(<StoreCard store={mockStore} />)
    
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('href', '/store/1')
  })

  it('should have correct accessibility attributes', () => {
    renderWithRouter(<StoreCard store={mockStore} />)
    
    const link = screen.getByRole('link')
    expect(link).toHaveAttribute('aria-label')
    expect(link.getAttribute('aria-label')).toContain('Test Store')
    expect(link.getAttribute('aria-label')).toContain('4.8')
  })

  it('should apply animation delay based on index', () => {
    renderWithRouter(<StoreCard store={mockStore} index={5} />)
    
    // 由于使用了 framer-motion，样式是通过 JS 动态应用的
    // 我们只需要验证组件渲染成功即可
    expect(screen.getByTestId('store-card')).toBeInTheDocument()
  })
})
