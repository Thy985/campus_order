import { Home, Store, ShoppingCart, User } from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useCartStore } from '@/stores/cartStore';
import { cn } from '@/lib/utils';

export function BottomNav() {
  const { getTotalCount } = useCartStore();
  const cartCount = getTotalCount();

  const navItems = [
    { path: '/', label: '首页', icon: Home },
    { path: '/stores', label: '商家', icon: Store },
    { path: '/cart', label: '购物车', icon: ShoppingCart, badge: cartCount },
    { path: '/profile', label: '我的', icon: User },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100 shadow-bottom lg:hidden">
      <div className="flex items-center justify-around h-16">
        {navItems.map((item) => {
          const Icon = item.icon;
          const badgeCount = item.badge || 0;
          
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                cn(
                  'flex flex-col items-center justify-center flex-1 h-full relative transition-colors duration-200',
                  isActive ? 'text-orange-500' : 'text-gray-400 hover:text-gray-600'
                )
              }
            >
              <div className="relative">
                <Icon className="w-6 h-6 transition-transform duration-200 ui-active:scale-110" />
                {badgeCount > 0 && (
                  <span className="absolute -top-2 -right-3 h-4 min-w-4 flex items-center justify-center p-0 px-1 text-[10px] bg-orange-500 text-white rounded-full">
                    {badgeCount > 99 ? '99+' : badgeCount}
                  </span>
                )}
              </div>
              <span className="text-xs mt-1 font-medium">{item.label}</span>
            </NavLink>
          );
        })}
      </div>
    </nav>
  );
}
