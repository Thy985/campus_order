import { useState } from 'react';
import { ShoppingBag, ChevronLeft, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { OrderCard } from '@/components/common/OrderCard';
import { useOrders } from '@/hooks';
import { PageSkeleton } from '@/components/ui/PageSkeleton';
import { OrderStatusText, OrderStatus } from '@/types';

const orderTabs: { value: number | 'all'; label: string }[] = [
  { value: 'all', label: '全部' },
  { value: OrderStatus.PENDING_PAYMENT, label: '待支付' },
  { value: OrderStatus.PENDING_ACCEPTANCE, label: '待接单' },
  { value: OrderStatus.PREPARING, label: '制作中' },
  { value: OrderStatus.READY_FOR_PICKUP, label: '待取餐' },
  { value: OrderStatus.COMPLETED, label: '已完成' },
];

export function Orders() {
  const [activeTab, setActiveTab] = useState<number | 'all'>('all');
  
  const { orders, loading, error, total, refresh } = useOrders({
    page: 1,
    pageSize: 20,
    status: activeTab,
  });

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-30 bg-white border-b border-gray-100">
        <div className="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
          <a href="#/" className="flex items-center gap-2 text-gray-700">
            <ChevronLeft className="w-5 h-5" />
            <span className="font-medium">返回</span>
          </a>
          <h1 className="text-lg font-semibold">我的订单</h1>
          <button 
            onClick={refresh}
            className="p-2 text-gray-500 hover:text-gray-700"
            disabled={loading}
          >
            <RefreshCw className={`w-5 h-5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </header>

      <div className="sticky top-14 z-20 bg-white border-b border-gray-100">
        <div className="max-w-3xl mx-auto">
          <Tabs value={String(activeTab)} onValueChange={(v) => setActiveTab(v === 'all' ? 'all' : Number(v))}>
            <TabsList className="w-full h-12 bg-transparent p-0 rounded-none">
              {orderTabs.map((tab) => (
                <TabsTrigger
                  key={tab.value}
                  value={String(tab.value)}
                  className="flex-1 h-full rounded-none data-[state=active]:border-b-2 data-[state=active]:border-orange-500 data-[state=active]:text-orange-500 data-[state=active]:shadow-none"
                >
                  {tab.label}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 py-4">
        {loading ? (
          <PageSkeleton />
        ) : error ? (
          <div className="text-center py-12">
            <ShoppingBag className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500">加载失败：{error.message}</p>
            <Button 
              variant="outline" 
              className="mt-4"
              onClick={refresh}
            >
              重试
            </Button>
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <ShoppingBag className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500">
              {activeTab === 'all' ? '暂无订单' : `暂无${OrderStatusText[activeTab as OrderStatus]}订单`}
            </p>
            <a href="#/">
              <Button className="mt-4 rounded-xl">去逛逛</Button>
            </a>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
