import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { Button, buttonVariants } from '@/components/ui/button'

describe('Button', () => {
  it('should render with default variant and size', () => {
    render(<Button>Click me</Button>)
    
    const button = screen.getByRole('button', { name: 'Click me' })
    expect(button).toBeInTheDocument()
    expect(button).toHaveAttribute('data-variant', 'default')
    expect(button).toHaveAttribute('data-size', 'default')
  })

  it('should render with different variants', () => {
    const { rerender } = render(<Button variant="destructive">Delete</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-variant', 'destructive')
    
    rerender(<Button variant="outline">Outline</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-variant', 'outline')
    
    rerender(<Button variant="ghost">Ghost</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-variant', 'ghost')
    
    rerender(<Button variant="link">Link</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-variant', 'link')
  })

  it('should render with different sizes', () => {
    const { rerender } = render(<Button size="sm">Small</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-size', 'sm')
    
    rerender(<Button size="lg">Large</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-size', 'lg')
    
    rerender(<Button size="icon">Icon</Button>)
    expect(screen.getByRole('button')).toHaveAttribute('data-size', 'icon')
  })

  it('should handle click events', () => {
    let clicked = false
    render(<Button onClick={() => { clicked = true }}>Click me</Button>)
    
    fireEvent.click(screen.getByRole('button'))
    expect(clicked).toBe(true)
  })

  it('should be disabled when disabled prop is true', () => {
    render(<Button disabled>Disabled</Button>)
    
    const button = screen.getByRole('button')
    expect(button).toBeDisabled()
  })

  it('should apply custom className', () => {
    render(<Button className="custom-class">Custom</Button>)
    
    const button = screen.getByRole('button')
    expect(button).toHaveClass('custom-class')
  })

  it('should forward ref correctly', () => {
    const ref = { current: null as HTMLButtonElement | null }
    render(<Button ref={ref}>With Ref</Button>)
    
    expect(ref.current).toBeInstanceOf(HTMLButtonElement)
  })

  it('should pass through additional props', () => {
    render(<Button type="submit" name="submit-btn">Submit</Button>)
    
    const button = screen.getByRole('button')
    expect(button).toHaveAttribute('type', 'submit')
    expect(button).toHaveAttribute('name', 'submit-btn')
  })
})

describe('buttonVariants', () => {
  it('should generate default variant classes', () => {
    const classes = buttonVariants()
    expect(classes).toContain('inline-flex')
    expect(classes).toContain('bg-primary')
  })

  it('should generate variant-specific classes', () => {
    const destructiveClasses = buttonVariants({ variant: 'destructive' })
    expect(destructiveClasses).toContain('bg-destructive')
    
    const outlineClasses = buttonVariants({ variant: 'outline' })
    expect(outlineClasses).toContain('border')
  })

  it('should generate size-specific classes', () => {
    const smClasses = buttonVariants({ size: 'sm' })
    expect(smClasses).toContain('h-8')
    
    const lgClasses = buttonVariants({ size: 'lg' })
    expect(lgClasses).toContain('h-10')
  })

  it('should combine variant and size classes', () => {
    const classes = buttonVariants({ variant: 'outline', size: 'lg' })
    expect(classes).toContain('border')
    expect(classes).toContain('h-10')
  })
})
