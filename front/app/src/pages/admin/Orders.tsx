import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Eye, ChevronLeft, ChevronRight } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useAdminOrders } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { OrderStatusText, OrderStatus } from '@/types';

const orderTabs: { value: number | 'all'; label: string }[] = [
  { value: 'all', label: '全部' },
  { value: OrderStatus.PENDING_PAYMENT, label: '待支付' },
  { value: OrderStatus.PENDING_ACCEPTANCE, label: '待接单' },
  { value: OrderStatus.PREPARING, label: '制作中' },
  { value: OrderStatus.READY_FOR_PICKUP, label: '待取餐' },
  { value: OrderStatus.COMPLETED, label: '已完成' },
  { value: OrderStatus.CANCELLED, label: '已取消' },
];

export function AdminOrders() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<number | 'all'>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(1);
  
  const { orders, loading, error, total, refresh } = useAdminOrders({
    page,
    pageSize: 10,
  });

  const handleViewOrder = (orderId: number) => {
    navigate(`/order/${orderId}`);
  };

  const filteredOrders = orders.filter((order) => {
    const matchesTab = activeTab === 'all' || order.status === activeTab;
    const matchesSearch = 
      order.orderNo.toLowerCase().includes(searchQuery.toLowerCase()) ||
      order.merchantName.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTab && matchesSearch;
  });

  const getStatusBadge = (status: number) => {
    const config: Record<number, { label: string; className: string }> = {
      [OrderStatus.PENDING_PAYMENT]: { label: '待支付', className: 'bg-orange-100 text-orange-600' },
      [OrderStatus.PENDING_ACCEPTANCE]: { label: '待接单', className: 'bg-blue-100 text-blue-600' },
      [OrderStatus.PREPARING]: { label: '制作中', className: 'bg-yellow-100 text-yellow-600' },
      [OrderStatus.READY_FOR_PICKUP]: { label: '待取餐', className: 'bg-purple-100 text-purple-600' },
      [OrderStatus.COMPLETED]: { label: '已完成', className: 'bg-green-100 text-green-600' },
      [OrderStatus.CANCELLED]: { label: '已取消', className: 'bg-gray-100 text-gray-600' },
      [OrderStatus.REFUSED]: { label: '已拒绝', className: 'bg-red-100 text-red-600' },
    };
    const configItem = config[status] || { label: OrderStatusText[status as OrderStatus] || '未知', className: 'bg-gray-100' };
    return <Badge className={configItem.className}>{configItem.label}</Badge>;
  };

  if (loading) {
    return (
      <div className="p-6">
        <PageSkeleton />
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">订单监控</h1>
          <p className="text-gray-500 mt-1">共 {total} 个订单</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="搜索订单号或商家..."
              className="pl-10 w-64"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <Button variant="outline" size="icon">
            <Filter className="w-4 h-4" />
          </Button>
        </div>
      </div>

      <Tabs value={String(activeTab)} onValueChange={(v) => setActiveTab(v === 'all' ? 'all' : Number(v))}>
        <TabsList className="w-full justify-start flex-wrap h-auto">
          {orderTabs.map((tab) => (
            <TabsTrigger key={tab.value} value={String(tab.value)} className="mb-1">
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <Card>
        <CardHeader>
          <CardTitle>订单列表</CardTitle>
        </CardHeader>
        <CardContent>
          {filteredOrders.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500">暂无订单数据</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="text-left py-3 px-4 font-medium text-gray-500">订单号</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">商家</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">商品</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">金额</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">状态</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">下单时间</th>
                    <th className="text-left py-3 px-4 font-medium text-gray-500">操作</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-gray-50">
                      <td className="py-4 px-4 font-medium text-gray-900">#{order.orderNo}</td>
                      <td className="py-4 px-4 text-gray-600">{order.merchantName}</td>
                      <td className="py-4 px-4 text-gray-600">
                        {order.items.map(item => `${item.name} x${item.quantity}`).join('，')}
                      </td>
                      <td className="py-4 px-4 font-medium text-orange-500">
                        ¥{order.totalAmount}
                      </td>
                      <td className="py-4 px-4">{getStatusBadge(order.status)}</td>
                      <td className="py-4 px-4 text-gray-500">
                        {new Date(order.createTime).toLocaleString()}
                      </td>
                      <td className="py-4 px-4">
                        <Button 
                          variant="ghost" 
                          size="sm"
                          onClick={() => handleViewOrder(order.id)}
                        >
                          <Eye className="w-4 h-4 mr-1" />
                          查看
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {total > 10 && (
        <div className="flex items-center justify-between">
          <p className="text-sm text-gray-500">
            显示第 {(page - 1) * 10 + 1} 到 {Math.min(page * 10, total)} 条，共 {total} 条
          </p>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => Math.max(1, p - 1))}
              disabled={page === 1}
            >
              <ChevronLeft className="w-4 h-4" />
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(p => p + 1)}
              disabled={page * 10 >= total}
            >
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
