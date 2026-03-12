import { useState, useRef, MouseEvent, type ButtonHTMLAttributes, type ReactNode } from 'react';
import { cn } from '@/lib/utils';
import { Button } from './button';

/**
 * RippleButton 组件
 * 带有波纹点击效果的按钮
 */

interface Ripple {
  id: number;
  x: number;
  y: number;
}

interface RippleButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  rippleColor?: string;
}

function RippleButton({
  children,
  className,
  rippleColor = 'rgba(255, 255, 255, 0.3)',
  onClick,
  ...props
}: RippleButtonProps) {
  const [ripples, setRipples] = useState<Ripple[]>([]);
  const buttonRef = useRef<HTMLButtonElement>(null);

  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    const button = buttonRef.current;
    if (!button) return;

    const rect = button.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const newRipple: Ripple = {
      id: Date.now(),
      x,
      y,
    };

    setRipples((prev) => [...prev, newRipple]);

    // 移除波纹
    setTimeout(() => {
      setRipples((prev) => prev.filter((r) => r.id !== newRipple.id));
    }, 600);

    onClick?.(e);
  };

  return (
    <Button
      ref={buttonRef}
      className={cn(
        'relative overflow-hidden transition-transform active:scale-95',
        className
      )}
      onClick={handleClick}
      {...props}
    >
      {ripples.map((ripple) => (
        <span
          key={ripple.id}
          className="absolute rounded-full animate-ripple pointer-events-none"
          style={{
            left: ripple.x,
            top: ripple.y,
            width: 100,
            height: 100,
            marginLeft: -50,
            marginTop: -50,
            backgroundColor: rippleColor,
          }}
        />
      ))}
      <span className="relative z-10">{children}</span>
    </Button>
  );
}

export { RippleButton };
export type { RippleButtonProps };
