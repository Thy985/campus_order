import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Trash2, Plus, Minus, ShoppingBag, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { useCartStore } from '@/stores/cartStore';
import { useUser } from '@/hooks';

export function Cart() {
  const navigate = useNavigate();
  const { cart, removeItem, updateQuantity, clearCart } = useCartStore();
  const { isAuthenticated } = useUser();
  const items = cart.items;
  const [loading, setLoading] = useState(false);

  // 按商家分组
  const groupedItems = items.reduce((groups, item) => {
    if (!groups[item.merchantId]) {
      groups[item.merchantId] = {
        merchantId: item.merchantId,
        merchantName: item.merchantName,
        items: [],
      };
    }
    groups[item.merchantId].items.push(item);
    return groups;
  }, {} as Record<number, { merchantId: number; merchantName: string; items: typeof items }>);

  const handleQuantityChange = (productId: number, delta: number) => {
    const item = items.find(i => i.productId === productId);
    if (item) {
      const newQuantity = item.quantity + delta;
      if (newQuantity <= 0) {
        removeItem(productId);
      } else {
        updateQuantity(productId, newQuantity);
      }
    }
  };

  const handleRemove = (productId: number) => {
    removeItem(productId);
    toast.success('已移除商品');
  };

  const handleCheckout = (merchantId: number) => {
    const merchantGroup = groupedItems[merchantId];
    if (!merchantGroup || merchantGroup.items.length === 0) return;

    // 检查用户是否登录
    if (!isAuthenticated) {
      toast.error('请先登录');
      navigate('/login', { state: { from: `/cart` } });
      return;
    }

    // 跳转到订单确认页面，传递商家ID
    navigate(`/checkout?merchantId=${merchantId}`);
  };

  // 空购物车状态
  if (items.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col">
        {/* Header */}
        <header className="sticky top-16 bg-white border-b z-10">
          <div className="max-w-3xl mx-auto px-4 h-14 flex items-center">
            <Link to="/" className="flex items-center gap-2 text-gray-600 hover:text-gray-900">
              <ArrowLeft className="w-5 h-5" />
              <span>继续购物</span>
            </Link>
            <h1 className="flex-1 text-center font-semibold">购物车</h1>
            <div className="w-16" />
          </div>
        </header>

        {/* Empty State */}
        <div className="flex-1 flex flex-col items-center justify-center p-8">
          <div className="w-24 h-24 rounded-full bg-gray-100 flex items-center justify-center mb-4">
            <ShoppingBag className="w-12 h-12 text-gray-400" />
          </div>
          <h2 className="text-lg font-medium text-gray-900 mb-2">购物车是空的</h2>
          <p className="text-gray-500 mb-6">快去挑选心仪的美食吧</p>
          <Link to="/stores">
            <Button className="rounded-xl">去逛逛</Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pb-32">
      {/* Header */}
      <header className="sticky top-16 bg-white border-b z-10">
        <div className="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-gray-600 hover:text-gray-900">
            <ArrowLeft className="w-5 h-5" />
            <span className="hidden sm:inline">继续购物</span>
          </Link>
          <h1 className="font-semibold">购物车 ({items.length})</h1>
          <button
            onClick={() => {
              if (confirm('确定清空购物车吗？')) {
                clearCart();
                toast.success('购物车已清空');
              }
            }}
            className="text-sm text-red-500 hover:text-red-600"
          >
            清空
          </button>
        </div>
      </header>

      {/* Cart Items */}
      <div className="max-w-3xl mx-auto px-4 py-4 space-y-4">
        {Object.values(groupedItems).map((group) => (
          <Card key={group.merchantId} className="overflow-hidden">
            {/* Merchant Header */}
            <div className="px-4 py-3 bg-gray-50 border-b flex items-center justify-between">
              <Link
                to={`/store/${group.merchantId}`}
                className="font-medium text-gray-900 hover:text-orange-500"
              >
                {group.merchantName}
              </Link>
            </div>

            {/* Items */}
            <div className="divide-y">
              {group.items.map((item) => (
                <div key={item.productId} className="p-4 flex gap-4" data-testid="cart-item">
                  {/* Product Image */}
                  <div className="w-20 h-20 rounded-lg bg-gray-100 flex-shrink-0 overflow-hidden">
                    {item.image ? (
                      <img
                        src={item.image}
                        alt={item.name}
                        className="w-full h-full object-cover"
                        loading="lazy"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-400">
                        <ShoppingBag className="w-8 h-8" />
                      </div>
                    )}
                  </div>

                  {/* Product Info */}
                  <div className="flex-1 min-w-0">
                    <h3 className="font-medium text-gray-900 truncate">{item.name}</h3>
                    <p className="text-orange-500 font-semibold mt-1">
                      ¥{(item.price || 0).toFixed(2)}
                    </p>

                    {/* Quantity Controls */}
                    <div className="flex items-center justify-between mt-2">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handleQuantityChange(item.productId, -1)}
                          className="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center hover:bg-gray-50"
                          aria-label="减少数量"
                        >
                          <Minus className="w-4 h-4" />
                        </button>
                        <span className="w-8 text-center font-medium">{item.quantity}</span>
                        <button
                          onClick={() => handleQuantityChange(item.productId, 1)}
                          className="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center hover:bg-gray-50"
                          aria-label="增加数量"
                        >
                          <Plus className="w-4 h-4" />
                        </button>
                      </div>

                      <button
                        onClick={() => handleRemove(item.productId)}
                        className="p-2 text-gray-400 hover:text-red-500 transition-colors"
                        aria-label="删除商品"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Merchant Footer */}
            <div className="px-4 py-3 bg-gray-50 border-t flex items-center justify-between">
              <div>
                <span className="text-sm text-gray-500">共 {group.items.length} 件商品</span>
                <p className="text-lg font-bold text-gray-900">
                  ¥{group.items.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0).toFixed(2)}
                </p>
              </div>
              <Button
                onClick={() => handleCheckout(group.merchantId)}
                disabled={loading}
                className="rounded-xl"
              >
                {loading ? '处理中...' : '去结算'}
              </Button>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
