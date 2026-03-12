import { useState, useEffect } from 'react';
import { Search, ShoppingCart, User, Menu, X, Store } from 'lucide-react';
import { NavLink, Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useCartStore } from '@/stores/cartStore';
import { useUserStore } from '@/stores/userStore';
import { cn } from '@/lib/utils';

export function UserHeader() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { getTotalCount } = useCartStore();
  const cartCount = getTotalCount();
  const { user, isAuthenticated } = useUserStore();

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navItems = [
    { path: '/', label: '首页' },
    { path: '/stores', label: '商家' },
    { path: '/orders', label: '订单' },
    { path: '/profile', label: '我的' },
  ];

  return (
    <header
      className={cn(
        'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
        isScrolled ? 'glass shadow-nav' : 'bg-transparent'
      )}
      role="banner"
    >
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 lg:h-18">
          {/* Logo */}
          <Link
            to="/"
            className="flex items-center gap-2 group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-orange-500 focus-visible:ring-offset-2 rounded-xl"
            aria-label="校园点餐 - 返回首页"
          >
            <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center shadow-primary group-hover:scale-105 transition-transform">
              <Store className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold text-gray-900 hidden sm:block">
              校园点餐
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center gap-1" role="navigation">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  cn(
                    'px-4 py-2 rounded-xl text-sm font-medium transition-colors',
                    isActive
                      ? 'text-orange-500 bg-orange-50'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                  )
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          {/* Right Actions */}
          <div className="flex items-center gap-2">
            {/* Search */}
            <button
              className="w-10 h-10 rounded-xl flex items-center justify-center text-gray-600 hover:bg-gray-100 transition-colors"
              aria-label="搜索"
            >
              <Search className="w-5 h-5" />
            </button>

            {/* Cart */}
            <Link
              to="/cart"
              className="relative w-10 h-10 rounded-xl flex items-center justify-center text-gray-600 hover:bg-gray-100 transition-colors"
              aria-label="购物车"
            >
              <ShoppingCart className="w-5 h-5" />
              {cartCount > 0 && (
                <span className="absolute -top-1 -right-1 h-5 min-w-5 flex items-center justify-center p-0 px-1 text-xs bg-orange-500 text-white rounded-full">
                  {cartCount > 99 ? '99+' : cartCount}
                </span>
              )}
            </Link>

            {/* User */}
            {isAuthenticated ? (
              <Link
                to="/profile"
                className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-xl hover:bg-gray-100 transition-colors"
              >
                <div className="w-8 h-8 rounded-full bg-orange-100 flex items-center justify-center">
                  <User className="w-4 h-4 text-orange-500" />
                </div>
                <span className="text-sm font-medium text-gray-700 hidden sm:block">
                  {user?.nickname || user?.phone}
                </span>
              </Link>
            ) : (
              <Link to="/login">
                <Button
                  variant="outline"
                  size="sm"
                  className="rounded-xl border-orange-200 text-orange-500 hover:bg-orange-50"
                >
                  登录
                </Button>
              </Link>
            )}

            {/* Mobile Menu Button */}
            <button
              className="lg:hidden w-10 h-10 rounded-xl flex items-center justify-center text-gray-600 hover:bg-gray-100 transition-colors"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              aria-label={isMobileMenuOpen ? '关闭菜单' : '打开菜单'}
              aria-expanded={isMobileMenuOpen}
            >
              {isMobileMenuOpen ? (
                <X className="w-5 h-5" />
              ) : (
                <Menu className="w-5 h-5" />
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {isMobileMenuOpen && (
        <div className="lg:hidden absolute top-full left-0 right-0 bg-white border-b border-gray-100 shadow-lg">
          <nav className="px-4 py-3 space-y-1" role="navigation">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  cn(
                    'block px-4 py-3 rounded-xl text-base font-medium transition-colors',
                    isActive
                      ? 'text-orange-500 bg-orange-50'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                  )
                }
                onClick={() => setIsMobileMenuOpen(false)}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        </div>
      )}
    </header>
  );
}
