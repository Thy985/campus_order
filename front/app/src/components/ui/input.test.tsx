import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Input } from '@/components/ui/input'

describe('Input', () => {
  it('should render an input element', () => {
    render(<Input placeholder="Enter text" />)
    
    const input = screen.getByPlaceholderText('Enter text')
    expect(input).toBeInTheDocument()
    expect(input).toHaveAttribute('data-slot', 'input')
  })

  it('should handle different input types', () => {
    const { rerender } = render(<Input type="text" />)
    expect(screen.getByRole('textbox')).toHaveAttribute('type', 'text')
    
    rerender(<Input type="password" placeholder="Password" />)
    expect(screen.getByPlaceholderText('Password')).toHaveAttribute('type', 'password')
    
    rerender(<Input type="email" placeholder="Email" />)
    expect(screen.getByPlaceholderText('Email')).toHaveAttribute('type', 'email')
    
    rerender(<Input type="number" placeholder="Number" />)
    expect(screen.getByPlaceholderText('Number')).toHaveAttribute('type', 'number')
  })

  it('should handle value changes', () => {
    const handleChange = vi.fn()
    render(<Input onChange={handleChange} />)
    
    const input = screen.getByRole('textbox')
    fireEvent.change(input, { target: { value: 'test value' } })
    
    expect(handleChange).toHaveBeenCalled()
  })

  it('should be disabled when disabled prop is true', () => {
    render(<Input disabled placeholder="Disabled input" />)
    
    const input = screen.getByPlaceholderText('Disabled input')
    expect(input).toBeDisabled()
  })

  it('should apply custom className', () => {
    render(<Input className="custom-input" placeholder="Custom" />)
    
    const input = screen.getByPlaceholderText('Custom')
    expect(input).toHaveClass('custom-input')
  })

  it('should handle readonly attribute', () => {
    render(<Input readOnly placeholder="Readonly" />)
    
    const input = screen.getByPlaceholderText('Readonly')
    expect(input).toHaveAttribute('readonly')
  })

  it('should handle required attribute', () => {
    render(<Input required placeholder="Required" />)
    
    const input = screen.getByPlaceholderText('Required')
    expect(input).toBeRequired()
  })

  it('should handle maxLength attribute', () => {
    render(<Input maxLength={10} placeholder="Limited" />)
    
    const input = screen.getByPlaceholderText('Limited')
    expect(input).toHaveAttribute('maxLength', '10')
  })

  it('should handle aria-invalid attribute', () => {
    render(<Input aria-invalid="true" placeholder="Invalid" />)
    
    const input = screen.getByPlaceholderText('Invalid')
    expect(input).toHaveAttribute('aria-invalid', 'true')
  })

  it('should forward ref correctly', () => {
    const ref = { current: null as HTMLInputElement | null }
    render(<Input ref={ref} placeholder="Ref input" />)
    
    expect(ref.current).toBeInstanceOf(HTMLInputElement)
  })

  it('should handle focus and blur events', () => {
    const handleFocus = vi.fn()
    const handleBlur = vi.fn()
    render(<Input onFocus={handleFocus} onBlur={handleBlur} placeholder="Focus test" />)
    
    const input = screen.getByPlaceholderText('Focus test')
    fireEvent.focus(input)
    expect(handleFocus).toHaveBeenCalled()
    
    fireEvent.blur(input)
    expect(handleBlur).toHaveBeenCalled()
  })
})
