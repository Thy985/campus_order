import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, ChevronRight } from 'lucide-react';
import { Card } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { toast } from '@/lib/toast';
import { getOrders } from '@/api/order';
import type { Order } from '@/types';
import { OrderStatusText } from '@/types';

export function OrderList() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setIsLoading(true);
      const data = await getOrders();
      setOrders(data.orderList || []);
    } catch (error) {
      console.error('加载订单失败:', error);
      toast.error('加载订单失败');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">加载中...</div>
      </div>
    );
  }

  if (orders.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-8">
        <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center mb-4">
          <Package className="w-12 h-12 text-gray-400" />
        </div>
        <h2 className="text-lg font-medium text-gray-900 mb-2">暂无订单</h2>
        <p className="text-gray-500 mb-6">快去下单吧</p>
        <Link to="/stores">
          <Button className="rounded-xl">去逛逛</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* Header */}
      <header className="sticky top-16 bg-white border-b z-10">
        <div className="max-w-3xl mx-auto px-4 h-14 flex items-center justify-center">
          <h1 className="font-semibold">我的订单</h1>
        </div>
      </header>

      {/* Order List */}
      <div className="max-w-3xl mx-auto px-4 py-4 space-y-4">
        {orders.map((order) => (
          <Link key={order.id} to={`/order/${order.id}`}>
            <Card className="p-4 hover:shadow-md transition-shadow">
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <span className="font-medium">{order.merchantName}</span>
                  <ChevronRight className="w-4 h-4 text-gray-400" />
                </div>
                <Badge className="bg-orange-100 text-orange-600">
                  {OrderStatusText[order.status] || '未知'}
                </Badge>
              </div>
              
              <div className="space-y-2 mb-3">
                {order.items && order.items.length > 0 ? (
                  <>
                    {order.items.slice(0, 2).map((item, idx) => (
                      <div key={idx} className="flex justify-between text-sm">
                        <span className="text-gray-600">
                          {item.name || '未知商品'} x{item.quantity || 0}
                        </span>
                        <span className="text-gray-900">
                          ¥{((item.price || 0) * (item.quantity || 0)).toFixed(2)}
                        </span>
                      </div>
                    ))}
                    {order.items.length > 2 && (
                      <p className="text-sm text-gray-400">
                        等 {order.items.length} 件商品
                      </p>
                    )}
                  </>
                ) : (
                  <p className="text-sm text-gray-400">暂无商品信息</p>
                )}
              </div>

              <div className="flex items-center justify-between pt-3 border-t">
                <span className="text-sm text-gray-500">
                  {new Date(order.createTime).toLocaleDateString()}
                </span>
                <span className="font-semibold">
                  实付 ¥{(order.totalAmount || 0).toFixed(2)}
                </span>
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
}
