import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Search, Filter, MoreHorizontal, Eye,
  CheckCircle, XCircle, Clock, Package,
  ChevronLeft, ChevronRight
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useMerchantOrders, useAcceptOrder, useRejectOrder, useCompleteOrder } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { OrderStatusText, OrderStatus } from '@/types';
import type { Order } from '@/types';

const orderTabs: { value: string; label: string; status?: OrderStatus }[] = [
  { value: 'all', label: '全部订单' },
  { value: 'wait_accept', label: '待接单', status: OrderStatus.PENDING_ACCEPTANCE },
  { value: 'making', label: '制作中', status: OrderStatus.PREPARING },
  { value: 'wait_pickup', label: '待取餐', status: OrderStatus.READY_FOR_PICKUP },
  { value: 'completed', label: '已完成', status: OrderStatus.COMPLETED },
];

export function MerchantOrders() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(1);
  
  const { orders, loading, error, total, refresh } = useMerchantOrders({
    page,
    pageSize: 10,
  });
  
  const { acceptOrder } = useAcceptOrder();
  const { rejectOrder } = useRejectOrder();
  const { completeOrder } = useCompleteOrder();

  const handleViewOrder = (orderId: number) => {
    navigate(`/order/${orderId}`);
  };

  const filteredOrders = orders.filter((order) => {
    const activeTabConfig = orderTabs.find(t => t.value === activeTab);
    const matchesTab = activeTab === 'all' || (activeTabConfig?.status !== undefined && order.status === activeTabConfig.status);
    const matchesSearch = 
      order.orderNo.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (order.items && order.items.some(item => (item.name || '').toLowerCase().includes(searchQuery.toLowerCase())));
    return matchesTab && matchesSearch;
  });

  const handleAccept = async (orderId: number) => {
    const success = await acceptOrder(orderId);
    if (success) {
      refresh();
    }
  };

  const handleReject = async (orderId: number) => {
    const success = await rejectOrder(orderId, '商家拒单');
    if (success) {
      refresh();
    }
  };

  const handleComplete = async (orderId: number) => {
    const success = await completeOrder(orderId);
    if (success) {
      refresh();
    }
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
          <h1 className="text-2xl font-bold text-gray-900">订单管理</h1>
          <p className="text-gray-500 mt-1">共 {total} 个订单</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input
              placeholder="搜索订单号或商品..."
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

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="w-full justify-start">
          {orderTabs.map((tab) => (
            <TabsTrigger key={tab.value} value={tab.value}>
              {tab.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <Card>
        <CardContent className="p-0">
          {filteredOrders.length === 0 ? (
            <div className="text-center py-12">
              <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500">暂无订单</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-100">
              {filteredOrders.map((order) => (
                <OrderItem
                  key={order.id}
                  order={order}
                  onAccept={() => handleAccept(order.id)}
                  onReject={() => handleReject(order.id)}
                  onComplete={() => handleComplete(order.id)}
                  onView={() => handleViewOrder(order.id)}
                />
              ))}
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

interface OrderItemProps {
  order: Order;
  onAccept: () => void;
  onReject: () => void;
  onComplete: () => void;
  onView: () => void;
}

function OrderItem({ order, onAccept, onReject, onComplete, onView }: OrderItemProps) {
  const [showDetails, setShowDetails] = useState(false);

  const getStatusBadge = (status: number) => {
    return <Badge className="bg-blue-100 text-blue-600">{OrderStatusText[status] || status}</Badge>;
  };

  const getActionButtons = () => {
    switch (order.status) {
      case OrderStatus.PENDING_ACCEPTANCE:
        return (
          <div className="flex items-center gap-2">
            <Button size="sm" variant="outline" onClick={onReject}>
              <XCircle className="w-4 h-4 mr-1" />
              拒绝
            </Button>
            <Button size="sm" onClick={onAccept}>
              <CheckCircle className="w-4 h-4 mr-1" />
              接单
            </Button>
          </div>
        );
      case OrderStatus.PREPARING:
        return (
          <Button size="sm" onClick={onComplete}>
            <Package className="w-4 h-4 mr-1" />
            完成制作
          </Button>
        );
      default:
        return null;
    }
  };

  return (
    <div className="p-4 hover:bg-gray-50 transition-colors">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <span className="font-medium text-gray-900">#{order.orderNo}</span>
            {getStatusBadge(order.status)}
            <span className="text-sm text-gray-500">
              {new Date(order.createTime).toLocaleString()}
            </span>
          </div>
          <div className="mt-2 text-sm text-gray-600">
            {order.items && order.items.length > 0 
              ? order.items.map(item => `${item.name || '未知商品'} x${item.quantity || 0}`).join('，')
              : '暂无商品信息'}
          </div>
          <div className="mt-1 text-lg font-bold text-orange-500">
            ¥{(order.totalAmount || 0).toFixed(2)}
          </div>
        </div>
        <div className="flex items-center gap-2">
          {getActionButtons()}
          <Button
            variant="ghost"
            size="sm"
            onClick={onView}
            title="查看订单详情"
          >
            <Eye className="w-4 h-4" />
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setShowDetails(!showDetails)}
            title="展开/收起详情"
          >
            <MoreHorizontal className="w-4 h-4" />
          </Button>
        </div>
      </div>
      
      {showDetails && (
        <div className="mt-4 p-4 bg-gray-50 rounded-lg text-sm">
          <p><span className="text-gray-500">联系人：</span>{order.address?.contactName}</p>
          <p><span className="text-gray-500">电话：</span>{order.address?.contactPhone}</p>
          <p><span className="text-gray-500">备注：</span>{order.remark || '无'}</p>
        </div>
      )}
    </div>
  );
}
