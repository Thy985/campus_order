import { useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Clock, Package } from 'lucide-react';
import { motion } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { getOrderById, cancelOrder } from '@/api/order';
import { OrderTimeline } from '@/components/common/OrderTimeline';
import type { Order } from '@/types';
import { OrderStatus, OrderStatusText } from '@/types';

export function OrderDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadOrder(id);
    }
  }, [id]);

  const loadOrder = async (orderId: string | number) => {
    try {
      setIsLoading(true);
      const data = await getOrderById(orderId);
      setOrder(data);
    } catch (error) {
      console.error('加载订单详情失败:', error);
      toast.error('加载订单详情失败');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!order || !confirm('确定取消该订单吗？')) return;
    
    try {
      await cancelOrder(order.id);
      toast.success('订单已取消');
      loadOrder(order.id);
    } catch {
      toast.error('取消订单失败');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500">加载中...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <Package className="w-16 h-16 text-gray-400 mb-4" />
        <h2 className="text-lg font-medium text-gray-900">订单不存在</h2>
        <Link to="/orders" className="mt-4">
          <Button variant="outline" className="rounded-xl">返回订单列表</Button>
        </Link>
      </div>
    );
  }

  const statusText = OrderStatusText[order.status] || '未知状态';

  return (
    <div className="min-h-screen bg-gray-50 pb-8">
      {/* Header */}
      <header className="sticky top-16 bg-white border-b z-10">
        <div className="max-w-3xl mx-auto px-4 h-14 flex items-center">
          <button 
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>返回</span>
          </button>
          <h1 className="flex-1 text-center font-semibold">订单详情</h1>
          <div className="w-16" />
        </div>
      </header>

      <div className="max-w-3xl mx-auto px-4 py-4 space-y-4">
        {/* Order Timeline */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <Card className="p-6">
            <h3 className="font-semibold mb-6">订单进度</h3>
            <OrderTimeline
              status={order.status}
              createTime={order.createTime}
              payTime={order.payTime}
              acceptTime={order.acceptTime}
              finishTime={order.finishTime}
            />
          </Card>
        </motion.div>

        {/* Merchant Info */}
        <Card className="p-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-semibold">{order.merchantName}</h3>
              <p className="text-sm text-gray-500 mt-1">订单号: {order.orderNo}</p>
            </div>
            <Link to={`/store/${order.merchantId}`}>
              <Button variant="outline" size="sm" className="rounded-lg">
                再下一单
              </Button>
            </Link>
          </div>
        </Card>

        {/* Order Items */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4">订单商品</h3>
          <div className="space-y-3">
            {order.items.map((item, idx) => (
              <div key={idx} className="flex justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-gray-900">{item.name}</span>
                  <span className="text-gray-500">x{item.quantity}</span>
                </div>
                <span className="text-gray-900">
                  ¥{(item.price * item.quantity).toFixed(2)}
                </span>
              </div>
            ))}
          </div>
          <div className="border-t mt-4 pt-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">商品总价</span>
              <span>¥{order.totalAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">配送费</span>
              <span>¥0.00</span>
            </div>
            <div className="flex justify-between font-semibold text-lg pt-2 border-t">
              <span>实付金额</span>
              <span className="text-orange-500">¥{order.totalAmount.toFixed(2)}</span>
            </div>
          </div>
        </Card>

        {/* Delivery Info */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4">配送信息</h3>
          <div className="space-y-3">
            <div className="flex items-start gap-3">
              <MapPin className="w-5 h-5 text-gray-400 mt-0.5" />
              <div>
                <p className="font-medium">{order.address?.contactName} {order.address?.contactPhone}</p>
                <p className="text-sm text-gray-500 mt-1">{order.address?.detail}</p>
              </div>
            </div>
            {order.remark && (
              <div className="flex items-start gap-3">
                <Clock className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-sm text-gray-500">备注: {order.remark}</p>
                </div>
              </div>
            )}
          </div>
        </Card>

        {/* Actions */}
        {order.status === OrderStatus.PENDING_PAYMENT && (
          <div className="flex gap-3">
            <Button 
              variant="outline" 
              className="flex-1 rounded-xl"
              onClick={handleCancel}
            >
              取消订单
            </Button>
            <Button className="flex-1 rounded-xl">
              立即支付
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
