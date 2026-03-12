import { useCallback } from 'react';
import './A11yComponents';

// 可访问性钩子

interface AccessibleClickProps {
  role: 'button';
  tabIndex: number;
  onKeyDown: (e: React.KeyboardEvent) => void;
  className: string;
}

export function useA11y() {
  // 屏幕阅读器通知
  const announce = useCallback((message: string, priority: 'polite' | 'assertive' = 'polite') => {
    const announcement = document.createElement('div');
    announcement.setAttribute('role', 'status');
    announcement.setAttribute('aria-live', priority);
    announcement.setAttribute('aria-atomic', 'true');
    announcement.className = 'sr-only';
    announcement.textContent = message;
    
    document.body.appendChild(announcement);
    
    setTimeout(() => {
      document.body.removeChild(announcement);
    }, 1000);
  }, []);

  // 获取可点击元素的可访问性属性
  const getAccessibleClickProps = useCallback((
    onClick: () => void
  ): AccessibleClickProps => ({
    role: 'button',
    tabIndex: 0,
    onKeyDown: (e: React.KeyboardEvent) => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        onClick();
      }
    },
    className: 'cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-orange-500 focus-visible:ring-offset-2',
  }), []);

  // 焦点管理
  const focusFirstElement = useCallback((container: HTMLElement | null) => {
    if (!container) return;
    
    const focusableElements = container.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    
    if (focusableElements.length > 0) {
      focusableElements[0].focus();
    }
  }, []);

  // 焦点陷阱（用于模态框）
  const trapFocus = useCallback((container: HTMLElement | null) => {
    if (!container) return () => {};

    const focusableElements = container.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    );
    
    const firstElement = focusableElements[0];
    const lastElement = focusableElements[focusableElements.length - 1];

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Tab') return;

      if (e.shiftKey) {
        if (document.activeElement === firstElement) {
          e.preventDefault();
          lastElement?.focus();
        }
      } else {
        if (document.activeElement === lastElement) {
          e.preventDefault();
          firstElement?.focus();
        }
      }
    };

    container.addEventListener('keydown', handleKeyDown);
    return () => container.removeEventListener('keydown', handleKeyDown);
  }, []);

  return {
    announce,
    getAccessibleClickProps,
    focusFirstElement,
    trapFocus,
  };
}
