import { 
  LayoutDashboard, 
  Utensils, 
  ClipboardList, 
  BarChart3, 
  Settings, 
  Store,
  Users,
  ShoppingBag,
  LogOut,
  ChevronRight
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useUserStore } from '@/stores/userStore';
import { cn } from '@/lib/utils';

interface SidebarProps {
  type: 'merchant' | 'admin';
}

export function Sidebar({ type }: SidebarProps) {
  const { logout, user } = useUserStore();
  const navigate = useNavigate();

  const merchantMenu = [
    { path: '/merchant/dashboard', label: '数据概览', icon: LayoutDashboard },
    { path: '/merchant/menu', label: '菜品管理', icon: Utensils },
    { path: '/merchant/orders', label: '订单处理', icon: ClipboardList },
    { path: '/merchant/statistics', label: '营业统计', icon: BarChart3 },
    { path: '/merchant/settings', label: '店铺设置', icon: Settings },
  ];

  const adminMenu = [
    { path: '/admin/dashboard', label: '系统概览', icon: LayoutDashboard },
    { path: '/admin/users', label: '用户管理', icon: Users },
    { path: '/admin/merchants', label: '商家管理', icon: Store },
    { path: '/admin/orders', label: '订单监控', icon: ShoppingBag },
    { path: '/admin/settings', label: '系统配置', icon: Settings },
  ];

  const menuItems = type === 'merchant' ? merchantMenu : adminMenu;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="fixed left-0 top-0 bottom-0 w-64 gradient-dark text-white z-50 hidden lg:flex flex-col">
      {/* Logo */}
      <div className="h-20 flex items-center px-6 border-b border-white/10">
        <NavLink to={type === 'merchant' ? '/merchant/dashboard' : '/admin/dashboard'} className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-orange-500 flex items-center justify-center">
            <Store className="w-5 h-5 text-white" />
          </div>
          <div>
            <span className="text-lg font-bold">{type === 'merchant' ? '商家中心' : '管理后台'}</span>
          </div>
        </NavLink>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto" role="navigation">
        {menuItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-4 py-3 rounded-xl transition-all group',
                  isActive
                    ? 'bg-orange-500 text-white'
                    : 'text-gray-400 hover:text-white hover:bg-white/10'
                )
              }
            >
              <Icon className="w-5 h-5" />
              <span className="font-medium">{item.label}</span>
              <ChevronRight className="w-4 h-4 ml-auto opacity-0 group-hover:opacity-100 transition-opacity" />
            </NavLink>
          );
        })}
      </nav>

      {/* User Info & Logout */}
      <div className="p-4 border-t border-white/10">
        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-full bg-orange-500 flex items-center justify-center">
            <span className="text-sm font-bold">{user?.nickname?.[0] || user?.phone?.[0] || 'U'}</span>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-white truncate">{user?.nickname || user?.phone || '用户'}</p>
            <p className="text-xs text-gray-400">{type === 'merchant' ? '商家账号' : '管理员'}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 w-full px-4 py-2.5 rounded-xl text-gray-400 hover:text-white hover:bg-white/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          <span className="text-sm font-medium">退出登录</span>
        </button>
      </div>
    </aside>
  );
}
