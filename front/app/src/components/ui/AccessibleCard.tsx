import { useA11y } from '@/hooks/useA11y';
import { cn } from '@/lib/utils';

interface AccessibleCardProps {
  children: React.ReactNode;
  onClick?: () => void;
  href?: string;
  label: string;
  className?: string;
  index?: number;
}

/**
 * 可访问的卡片组件
 * 支持点击、键盘导航和屏幕阅读器
 */
export function AccessibleCard({
  children,
  onClick,
  href,
  label,
  className,
  index = 0,
}: AccessibleCardProps) {
  const { getAccessibleClickProps } = useA11y();

  const baseClasses = cn(
    'group block bg-white rounded-2xl shadow-card overflow-hidden card-hover animate-fade-in-up',
    className
  );

  const animationStyle = { animationDelay: `${index * 80}ms` };

  // 如果是链接
  if (href) {
    return (
      <a
        href={href}
        className={baseClasses}
        style={animationStyle}
        aria-label={label}
      >
        {children}
      </a>
    );
  }

  // 如果是可点击的
  if (onClick) {
    const accessibleProps = getAccessibleClickProps(onClick);
    return (
      <div
        {...accessibleProps}
        className={cn(baseClasses, accessibleProps.className)}
        style={animationStyle}
        aria-label={label}
      >
        {children}
      </div>
    );
  }

  // 普通卡片
  return (
    <article className={baseClasses} style={animationStyle} aria-label={label}>
      {children}
    </article>
  );
}
