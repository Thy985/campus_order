import { useState } from 'react';
import { ChevronDown, ChevronUp, Clock, CheckCircle, Package, Store, ChefHat, XCircle, Ban } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { OrderStatus, OrderStatusText } from '@/types';
import type { Order } from '@/types';

interface OrderCardProps {
  order: Order;
  index?: number;
  onPay?: (orderId: number) => void;
  onConfirmPickup?: (orderId: number) => void;
  onReorder?: (order: Order) => void;
}

const statusStyles: Record<number, { bg: string; text: string; icon: React.ElementType }> = {
  [OrderStatus.PENDING_PAYMENT]: { bg: 'bg-amber-100', text: 'text-amber-600', icon: Clock },
  [OrderStatus.PENDING_ACCEPTANCE]: { bg: 'bg-blue-100', text: 'text-blue-600', icon: Store },
  [OrderStatus.PREPARING]: { bg: 'bg-yellow-100', text: 'text-yellow-600', icon: ChefHat },
  [OrderStatus.READY_FOR_PICKUP]: { bg: 'bg-emerald-100', text: 'text-emerald-600', icon: Package },
  [OrderStatus.COMPLETED]: { bg: 'bg-green-100', text: 'text-green-600', icon: CheckCircle },
  [OrderStatus.CANCELLED]: { bg: 'bg-gray-100', text: 'text-gray-600', icon: XCircle },
};

export function OrderCard({ order, index = 0, onPay, onConfirmPickup, onReorder }: OrderCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const statusStyle = statusStyles[order.status] || statusStyles[OrderStatus.PENDING_PAYMENT];
  const StatusIcon = statusStyle.icon;

  const getActionButton = () => {
    switch (order.status) {
      case OrderStatus.PENDING_PAYMENT:
        return (
          <Button 
            size="sm" 
            className="h-9 px-4 rounded-xl gradient-primary text-white shadow-primary"
            onClick={() => onPay?.(order.id)}
          >
            去支付
          </Button>
        );
      case OrderStatus.READY_FOR_PICKUP:
        return (
          <Button 
            size="sm" 
            className="h-9 px-4 rounded-xl gradient-primary text-white shadow-primary"
            onClick={() => onConfirmPickup?.(order.id)}
          >
            确认取餐
          </Button>
        );
      case OrderStatus.COMPLETED:
        return (
          <Button 
            size="sm" 
            variant="outline" 
            className="h-9 px-4 rounded-xl border-gray-200"
            onClick={() => onReorder?.(order)}
          >
            再来一单
          </Button>
        );
      default:
        return null;
    }
  };

  return (
    <div
      className="bg-white rounded-2xl border border-gray-100 overflow-hidden animate-fade-in-up"
      style={{ animationDelay: `${index * 80}ms` }}
      data-testid="order-card"
    >
      {/* Header */}
      <div className="p-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl overflow-hidden bg-gray-100">
            <img
              src={order.merchantLogo || '/placeholder-store.png'}
              alt={order.merchantName}
              className="w-full h-full object-cover"
            />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900">{order.merchantName}</h3>
            <p className="text-xs text-gray-400 mt-0.5">{order.createTime}</p>
          </div>
        </div>
        <Badge className={`${statusStyle.bg} ${statusStyle.text} border-0 flex items-center gap-1`}>
          <StatusIcon className="w-3.5 h-3.5" />
          {OrderStatusText[order.status] || '未知状态'}
        </Badge>
      </div>

      {/* Items Preview */}
      <div className="px-4">
        <div className="flex gap-3 overflow-x-auto pb-3 custom-scrollbar">
          {order.items && order.items.length > 0 ? (
            order.items.map((item, i) => (
              <div key={i} className="flex-shrink-0 flex items-center gap-2 bg-gray-50 rounded-xl p-2">
                <img
                  src={item.image || '/placeholder-dish.png'}
                  alt={item.name || '商品'}
                  className="w-12 h-12 rounded-lg object-cover"
                />
                <div className="min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate max-w-[100px]">
                    {item.name || '未知商品'}
                  </p>
                  <p className="text-xs text-gray-500">x{item.quantity || 0}</p>
                </div>
              </div>
            ))
          ) : (
            <div className="text-gray-500 text-sm">暂无商品信息</div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div className="p-4 pt-2 flex items-center justify-between border-t border-gray-50">
        <div className="flex items-baseline gap-2">
          <span className="text-sm text-gray-500">共{order.items?.reduce((sum, i) => sum + i.quantity, 0) || 0}件</span>
          <span className="text-sm text-gray-500">实付</span>
          <span className="text-lg font-bold text-orange-500">¥{(order.actualAmount || 0).toFixed(2)}</span>
        </div>
        <div className="flex items-center gap-2">
          {getActionButton()}
          <Button
            size="sm"
            variant="ghost"
            onClick={() => setIsExpanded(!isExpanded)}
            className="h-9 w-9 p-0 rounded-xl"
          >
            {isExpanded ? (
              <ChevronUp className="w-5 h-5" />
            ) : (
              <ChevronDown className="w-5 h-5" />
            )}
          </Button>
        </div>
      </div>

      {/* Expanded Details */}
      {isExpanded && (
        <div className="px-4 pb-4 border-t border-gray-50 animate-accordion-down">
          <div className="pt-4 space-y-3">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">订单编号</span>
              <span className="text-gray-900 font-mono">{order.orderNo}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">商品总额</span>
              <span className="text-gray-900">¥{(order.totalAmount || 0).toFixed(2)}</span>
            </div>
            {order.remark && (
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">备注</span>
                <span className="text-gray-900">{order.remark}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
