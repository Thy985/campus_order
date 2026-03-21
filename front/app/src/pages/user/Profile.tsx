import { MapPin, Heart, MessageSquare, Settings, HelpCircle, Gift, Bell, Crown, LogOut, User as UserIcon, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { useUser } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';

export function Profile() {
  const navigate = useNavigate();
  const { user, loading, isAuthenticated, logout } = useUser();

  // 加载中状态
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 pb-20">
        <PageSkeleton />
      </div>
    );
  }

  // 未登录状态
  if (!isAuthenticated || !user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white rounded-3xl shadow-lg p-8 text-center max-w-sm w-full">
          <div className="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-4">
            <UserIcon className="w-10 h-10 text-gray-400" />
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">未登录</h2>
          <p className="text-gray-500 mb-6">请先登录后查看个人中心</p>
          <Button
            className="w-full rounded-xl bg-gradient-to-r from-orange-500 to-red-500"
            onClick={() => navigate('/login')}
          >
            立即登录
          </Button>
        </div>
      </div>
    );
  }

  const menuGroups = [
    {
      title: '我的服务',
      items: [
        { icon: MapPin, label: '收货地址', path: '#/address' },
        { icon: Heart, label: '我的收藏', path: '#/favorites' },
        { icon: Gift, label: '优惠券', path: '#/coupons', badge: '3张' },
      ],
    },
    {
      title: '更多服务',
      items: [
        { icon: MessageSquare, label: '联系客服', path: '#/support' },
        { icon: HelpCircle, label: '帮助中心', path: '#/help' },
        { icon: Settings, label: '设置', path: '#/settings' },
      ],
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header Background */}
      <div className="relative h-48 gradient-primary">
        <div className="absolute inset-0 bg-gradient-to-b from-transparent to-black/20" />
        
        <div className="absolute top-0 right-0 p-4">
          <button className="w-10 h-10 rounded-xl bg-white/20 backdrop-blur flex items-center justify-center text-white">
            <Bell className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Profile Card */}
      <div className="max-w-3xl mx-auto px-4 -mt-20 relative z-10">
        <div className="bg-white rounded-3xl shadow-card-lg p-6">
          <div className="flex items-center gap-4">
            <div className="w-20 h-20 rounded-2xl overflow-hidden border-4 border-white shadow-lg">
              <img
                src={user.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + user.id}
                alt={user.nickname}
                className="w-full h-full object-cover"
              />
            </div>
            <div className="flex-1">
              <h2 className="text-xl font-bold text-gray-900">{user.nickname || user.phone}</h2>
              <p className="text-sm text-gray-500">{user.phone}</p>
              <div className="flex items-center gap-2 mt-2">
                <Badge className="bg-gradient-to-r from-orange-500 to-red-500 text-white border-0">
                  <Crown className="w-3 h-3 mr-1" />
                  普通会员
                </Badge>
              </div>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4 mt-6 pt-6 border-t border-gray-100">
            <div className="text-center">
              <div className="text-xl font-bold text-gray-900">0</div>
              <div className="text-sm text-gray-500">收藏</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-gray-900">0</div>
              <div className="text-sm text-gray-500">足迹</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-gray-900">0</div>
              <div className="text-sm text-gray-500">优惠券</div>
            </div>
          </div>
        </div>
      </div>

      {/* Menu Groups */}
      <div className="max-w-3xl mx-auto px-4 mt-6 space-y-6">
        {menuGroups.map((group) => (
          <div key={group.title} className="bg-white rounded-2xl shadow-card overflow-hidden">
            <h3 className="px-4 py-3 text-sm font-medium text-gray-500">{group.title}</h3>
            <div className="divide-y divide-gray-100">
              {group.items.map((item) => (
                <a
                  key={item.label}
                  href={item.path}
                  className="flex items-center justify-between px-4 py-4 hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-orange-50 flex items-center justify-center">
                      <item.icon className="w-4 h-4 text-orange-500" />
                    </div>
                    <span className="text-gray-700">{item.label}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    {item.badge && (
                      <Badge className="bg-orange-100 text-orange-600 border-0">
                        {item.badge}
                      </Badge>
                    )}
                    <ChevronRight className="w-5 h-5 text-gray-400" />
                  </div>
                </a>
              ))}
            </div>
          </div>
        ))}

        {/* Logout Button */}
        <Button
          variant="outline"
          className="w-full rounded-xl py-6 text-red-500 border-red-200 hover:bg-red-50"
          onClick={logout}
        >
          <LogOut className="w-5 h-5 mr-2" />
          退出登录
        </Button>
      </div>
    </div>
  );
}
