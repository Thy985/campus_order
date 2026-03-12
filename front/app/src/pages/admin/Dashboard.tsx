import { 
  Users, Store, ShoppingBag, 
  Activity, DollarSign, TrendingUp, Package
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useAdminStats, useScrollAnimation } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';

export function AdminDashboard() {
  const { ref } = useScrollAnimation<HTMLDivElement>();
  const { stats, loading, error } = useAdminStats();

  if (loading) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  if (error || !stats) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-red-500">加载失败：{error?.message || '未知错误'}</p>
        </div>
      </div>
    );
  }

  const statCards = [
    {
      title: '总用户数',
      value: stats.totalUsers.toLocaleString(),
      icon: Users,
      color: 'bg-blue-500',
    },
    {
      title: '入驻商家',
      value: stats.totalMerchants.toLocaleString(),
      icon: Store,
      color: 'bg-orange-500',
    },
    {
      title: '今日订单',
      value: stats.todayOrders.toLocaleString(),
      icon: ShoppingBag,
      color: 'bg-green-500',
    },
    {
      title: '今日营收',
      value: `¥${stats.todaySales.toLocaleString()}`,
      icon: DollarSign,
      color: 'bg-purple-500',
    },
  ];

  const orderStatusList = [
    { label: '待支付', count: 0, color: 'bg-amber-500' },
    { label: '待接单', count: 0, color: 'bg-blue-500' },
    { label: '制作中', count: 0, color: 'bg-purple-500' },
    { label: '待取餐', count: 0, color: 'bg-emerald-500' },
    { label: '已完成', count: 0, color: 'bg-gray-500' },
    { label: '已取消', count: 0, color: 'bg-red-500' },
  ];

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">系统概览</h1>
        <p className="text-gray-500 mt-1">查看平台整体运营数据</p>
      </div>

      <div ref={ref} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <Card
            key={stat.title}
            className="card-hover animate-fade-in-up"
            style={{ animationDelay: `${index * 80}ms` }}
          >
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div className={`w-12 h-12 rounded-xl ${stat.color} flex items-center justify-center`}>
                  <stat.icon className="w-6 h-6 text-white" />
                </div>
              </div>
              <div className="mt-4">
                <p className="text-sm text-gray-500">{stat.title}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="animate-fade-in-up" style={{ animationDelay: '320ms' }}>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-blue-500" />
              平台数据总览
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
                    <ShoppingBag className="w-5 h-5 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">累计订单</p>
                    <p className="font-bold text-gray-900">{stats.totalOrders.toLocaleString()}</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center">
                    <DollarSign className="w-5 h-5 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">累计营收</p>
                    <p className="font-bold text-gray-900">¥{stats.totalSales.toLocaleString()}</p>
                  </div>
                </div>
              </div>
              <div className="flex items-center justify-between p-4 bg-gray-50 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-orange-100 flex items-center justify-center">
                    <Package className="w-5 h-5 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">商品总数</p>
                    <p className="font-bold text-gray-900">{stats.totalProducts.toLocaleString()}</p>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="animate-fade-in-up" style={{ animationDelay: '400ms' }}>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Activity className="w-5 h-5 text-orange-500" />
              待处理事项
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-orange-50 rounded-xl">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-lg bg-orange-500 flex items-center justify-center">
                    <Package className="w-5 h-5 text-white" />
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">待处理订单</p>
                    <p className="font-bold text-gray-900">{stats.pendingOrders} 个订单需要处理</p>
                  </div>
                </div>
                <Badge className="bg-orange-500 text-white">
                  {stats.pendingOrders}
                </Badge>
              </div>
              <div className="text-center py-8 text-gray-500">
                <p className="text-sm">更多功能正在开发中...</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card className="animate-fade-in-up" style={{ animationDelay: '480ms' }}>
        <CardHeader>
          <CardTitle className="text-lg">订单状态分布</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
            {orderStatusList.map((item) => (
              <div key={item.label} className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                <div className={`w-3 h-3 rounded-full ${item.color}`} />
                <div>
                  <p className="text-lg font-bold text-gray-900">{item.count}</p>
                  <p className="text-sm text-gray-500">{item.label}</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
