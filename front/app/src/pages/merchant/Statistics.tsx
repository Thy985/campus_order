import { useState } from 'react';
import { TrendingUp, TrendingDown, Download, Calendar, Package } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useMerchantStats } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';

export function MerchantStatistics() {
  const [timeRange, setTimeRange] = useState<'week' | 'month' | 'year'>('week');
  
  const { stats, loading, error, refresh } = useMerchantStats();

  const timeRanges = [
    { value: 'week', label: '本周' },
    { value: 'month', label: '本月' },
    { value: 'year', label: '本年' },
  ];

  if (loading || !stats) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p className="text-red-500">加载失败：{error.message}</p>
          <Button className="mt-4" onClick={refresh}>重试</Button>
        </div>
      </div>
    );
  }

  // 如果没有数据，显示空状态
  if (!stats) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <p className="text-gray-500">暂无统计数据</p>
          <Button className="mt-4" onClick={refresh}>刷新</Button>
        </div>
      </div>
    );
  }

  const summaryCards = [
    {
      title: '营业额',
      value: `¥${stats.todayRevenue.toFixed(2)}`,
      change: '+12.5%',
      trend: 'up',
      total: `累计 ¥${stats.totalRevenue.toFixed(2)}`,
    },
    {
      title: '订单数',
      value: String(stats.todayOrders),
      change: '+8.3%',
      trend: 'up',
      total: `累计 ${stats.totalOrders} 单`,
    },
    {
      title: '客单价',
      value: stats.todayOrders > 0 
        ? `¥${(stats.todayRevenue / stats.todayOrders).toFixed(2)}` 
        : '¥0.00',
      change: '+5.2%',
      trend: 'up',
      total: '平均每单',
    },
    {
      title: '评分',
      value: String(stats.rating),
      change: '+0.1',
      trend: 'up',
      total: '5分制',
    },
  ];

  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">营业统计</h1>
          <p className="text-gray-500 mt-1">查看店铺经营数据分析</p>
        </div>
        <div className="flex gap-2">
          {timeRanges.map((range) => (
            <Button
              key={range.value}
              variant={timeRange === range.value ? 'default' : 'outline'}
              onClick={() => setTimeRange(range.value as typeof timeRange)}
              className={`h-10 px-4 rounded-xl ${timeRange === range.value ? 'bg-orange-500 hover:bg-orange-600' : ''}`}
            >
              {range.label}
            </Button>
          ))}
          <Button variant="outline" className="h-10 px-4 rounded-xl border-gray-200">
            <Download className="w-4 h-4 mr-2" />
            导出
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {summaryCards.map((card) => (
          <Card key={card.title} className="card-hover">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <span className="text-gray-500">{card.title}</span>
                <div className="flex items-center text-sm">
                  {card.trend === 'up' ? (
                    <TrendingUp className="w-4 h-4 mr-1" />
                  ) : (
                    <TrendingDown className="w-4 h-4 mr-1" />
                  )}
                  {card.change}
                </div>
              </div>
              <div className="mt-2">
                <span className="text-2xl font-bold text-gray-900">{card.value}</span>
              </div>
              <div className="mt-1 text-sm text-gray-400">{card.total}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-lg flex items-center gap-2">
              <Calendar className="w-5 h-5 text-blue-500" />
              营收趋势
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64 flex items-end justify-between gap-2">
              {stats.weeklySales.map((item, index) => {
                const maxSales = Math.max(...stats.weeklySales.map(s => s.salesAmount));
                const height = maxSales > 0 ? (item.salesAmount / maxSales) * 100 : 0;
                
                return (
                  <div key={item.day} className="flex-1 flex flex-col items-center gap-2">
                    <div className="relative w-full flex items-end justify-center" style={{ height: '200px' }}>
                      <div
                        className="w-full max-w-[40px] bg-blue-500 rounded-t-lg transition-all duration-500"
                        style={{ height: `${height}%` }}
                      />
                    </div>
                    <span className="text-xs text-gray-500">{item.day}</span>
                    <span className="text-xs text-gray-400">¥{item.salesAmount}</span>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-lg">热销菜品TOP5</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats.topDishes.map((dish, index) => {
                const maxSales = Math.max(...stats.topDishes.map(d => d.salesAmount));
                const percentage = maxSales > 0 ? (dish.salesAmount / maxSales) * 100 : 0;
                
                return (
                  <div key={dish.name} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className={`w-6 h-6 rounded flex items-center justify-center text-sm font-bold ${index < 3 ? 'bg-orange-500 text-white' : 'bg-gray-200 text-gray-600'}`}>
                          {index + 1}
                        </div>
                        <span className="font-medium text-gray-900">{dish.name}</span>
                      </div>
                      <span className="text-gray-600">¥{dish.salesAmount}</span>
                    </div>
                    <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                      <div 
                        className="h-full bg-orange-500 rounded-full transition-all duration-500"
                        style={{ width: `${percentage}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg">每日明细</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b border-gray-100">
                  <th className="text-left py-3 px-4 font-medium text-gray-500">日期</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">订单数</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">营业额</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500">客单价</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {stats.weeklySales.map((day) => (
                  <tr key={day.day} className="hover:bg-gray-50">
                    <td className="py-4 px-4">{day.day}</td>
                    <td className="py-4 px-4">{Math.floor(day.salesAmount / 25)} 单</td>
                    <td className="py-4 px-4 font-medium text-orange-500">¥{day.salesAmount}</td>
                    <td className="py-4 px-4">¥{(day.salesAmount / Math.floor(day.salesAmount / 25)).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
