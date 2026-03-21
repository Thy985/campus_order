import { 
  TrendingUp, ShoppingBag, Users, 
  Clock, DollarSign, ChefHat, Package 
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useMerchantStats, useMerchantOrders } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { OrderStatusText, OrderStatus } from '@/types';

export function MerchantDashboard() {
  const { stats, loading: statsLoading } = useMerchantStats();
  const { orders, loading: ordersLoading } = useMerchantOrders({
    page: 1,
    pageSize: 5,
  });

  const loading = statsLoading || ordersLoading;

  if (loading || !stats) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  const statCards = [
    {
      title: '今日订单',
      value: stats.todayOrders,
      change: '+12%',
      trend: 'up',
      icon: ShoppingBag,
      color: 'bg-blue-500',
    },
    {
      title: '今日营收',
      value: `¥${stats.todayRevenue.toFixed(2)}`,
      change: '+8%',
      trend: 'up',
      icon: DollarSign,
      color: 'bg-orange-500',
    },
    {
      title: '累计订单',
      value: stats.totalOrders,
      change: '+5%',
      trend: 'up',
      icon: Users,
      color: 'bg-green-500',
    },
    {
      title: '待处理订单',
      value: stats.pendingOrders,
      change: '需处理',
      trend: 'neutral',
      icon: Clock,
      color: 'bg-red-500',
    },
  ] as const;

  const pendingOrders = orders.filter(
    (o) => o.status === OrderStatus.PENDING_ACCEPTANCE || o.status === OrderStatus.PREPARING
  );

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">数据概览</h1>
        <p className="text-gray-500 mt-1">查看店铺今日经营数据</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((card, index) => (
          <Card
            key={card.title}
            className="card-hover animate-fade-in-up"
            style={{ animationDelay: `${index * 80}ms` }}
          >
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div className={`w-12 h-12 rounded-xl ${card.color} flex items-center justify-center`}>
                  <card.icon className="w-6 h-6 text-white" />
                </div>
                <Badge 
                  variant={card.trend === 'up' ? 'default' : 'secondary'}
                  className={card.trend === 'up' ? 'bg-green-100 text-green-600' : 'bg-orange-100 text-orange-600'}
                >
                  {card.change}
                </Badge>
              </div>
              <div className="mt-4">
                <p className="text-sm text-gray-500">{card.title}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{card.value}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-lg flex items-center gap-2">
                <Clock className="w-5 h-5 text-orange-500" />
                待处理订单
              </CardTitle>
              <Badge className="bg-orange-100 text-orange-600">
                {pendingOrders.length} 个待处理
              </Badge>
            </div>
          </CardHeader>
          <CardContent>
            {pendingOrders.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Package className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                <p>暂无待处理订单</p>
              </div>
            ) : (
              <div className="space-y-4">
                {pendingOrders.slice(0, 5).map((order) => (
                  <div
                    key={order.id}
                    className="flex items-center justify-between p-4 bg-gray-50 rounded-xl"
                  >
                    <div>
                      <p className="font-medium text-gray-900">订单 #{order.orderNo}</p>
                      <p className="text-sm text-gray-500 mt-1">
                        {order.items?.length || 0} 件商品 · ¥{(order.totalAmount || 0).toFixed(2)}
                      </p>
                    </div>
                    <Badge className="bg-orange-100 text-orange-600">
                      {OrderStatusText[order.status] || order.status}
                    </Badge>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <TrendingUp className="w-5 h-5 text-blue-500" />
              本周营收趋势
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64 flex items-end justify-between gap-2">
              {stats.weeklySales.map((item, index) => {
                const maxSales = Math.max(...stats.weeklySales.map(s => s.salesAmount));
                const height = maxSales > 0 ? (item.salesAmount / maxSales) * 100 : 0;
                
                return (
                  <div key={item.day} className="flex-1 flex flex-col items-center gap-2">
                    <div
                      className="w-full bg-blue-500 rounded-t-lg transition-all duration-500"
                      style={{ 
                        height: `${height}%`,
                        animationDelay: `${index * 100}ms`
                      }}
                    />
                    <span className="text-xs text-gray-500">{item.day}</span>
                    <span className="text-xs text-gray-400">¥{item.salesAmount}</span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <ChefHat className="w-5 h-5 text-orange-500" />
            热销菜品TOP5
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {stats.topDishes.map((dish, index) => (
              <div
                key={dish.name}
                className="flex items-center justify-between p-4 bg-gray-50 rounded-xl"
              >
                <div className="flex items-center gap-4">
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center text-sm font-bold ${index < 3 ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-600'}`}>
                    {index + 1}
                  </div>
                  <span className="font-medium text-gray-900">{dish.name}</span>
                </div>
                <div className="text-right">
                  <p className="font-medium text-gray-900">¥{dish.salesAmount}</p>
                  <p className="text-sm text-gray-500">销售额</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
