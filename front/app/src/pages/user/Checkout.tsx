import { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, ShoppingBag, MapPin, Clock, CreditCard, Wallet } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { toast } from '@/lib/toast';
import { useCartStore } from '@/stores';
import { useCreateOrder } from '@/hooks';
import { useUser } from '@/hooks';
import type { CartItem } from '@/stores';

export function Checkout() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [remark, setRemark] = useState('');
  const [deliveryAddress, setDeliveryAddress] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [contactName, setContactName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const { cart, getTotalPrice, clearCart, removeItem } = useCartStore();
  const { user } = useUser();
  const { createOrder, loading: createOrderLoading } = useCreateOrder();
  
  // 从URL参数获取商家ID，如果没有则使用购物车中的第一个商家
  const urlMerchantId = searchParams.get('merchantId');
  
  // 根据商家ID过滤购物车商品
  const checkoutItems = useMemo(() => {
    if (urlMerchantId) {
      return cart.items.filter(item => item.merchantId === Number(urlMerchantId));
    }
    return cart.items;
  }, [cart.items, urlMerchantId]);
  
  const merchantId = urlMerchantId ? Number(urlMerchantId) : cart.items[0]?.merchantId;
  const merchantName = checkoutItems[0]?.merchantName || '商家';
  
  const totalPrice = useMemo(() => {
    return checkoutItems.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0);
  }, [checkoutItems]);
  
  // 如果购物车为空或没有对应商家的商品，显示空状态
  if (checkoutItems.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <ShoppingBag className="w-16 h-16 text-gray-400 mb-4" />
        <h2 className="text-lg font-medium text-gray-900">购物车是空的</h2>
        <p className="text-gray-500 mt-2">请先添加商品</p>
        <Button 
          onClick={() => navigate('/')}
          className="mt-4 rounded-xl"
        >
          去逛逛
        </Button>
      </div>
    );
  }
  
  const handleSubmit = async () => {
    if (!user) {
      toast.error('请先登录');
      navigate('/login');
      return;
    }
    
    // 验证必填字段
    if (!deliveryAddress.trim()) {
      toast.error('请输入配送地址');
      return;
    }
    
    // 验证地址长度（最多100个字符）
    if (deliveryAddress.trim().length > 100) {
      toast.error('配送地址不能超过100个字符');
      return;
    }
    
    if (!contactPhone.trim()) {
      toast.error('请输入联系电话');
      return;
    }
    
    // 增强手机号验证
    if (!/^1[3-9]\d{9}$/.test(contactPhone)) {
      toast.error('请输入正确的11位手机号');
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      // 转换数据格式以匹配后端API
      const orderData = {
        merchantId: merchantId!,
        orderItems: checkoutItems.map((item) => ({
          productId: item.productId,
          productName: item.name || '未知商品',
          productPrice: item.price || 0,
          quantity: item.quantity || 0,
          productImage: item.image,
        })),
        remark: remark || undefined,
        deliveryAddress: deliveryAddress.trim(),
        contactPhone: contactPhone.trim(),
        contactName: contactName.trim() || undefined,
      };

      const order = await createOrder(orderData as any);
      console.log('createOrder 返回:', order);

      if (order) {
        console.log('跳转路径:', `/order/${order.id}`);
        checkoutItems.forEach(item => removeItem(item.productId));
        toast.success('订单创建成功');
        navigate(`/order/${order.id}`);
      } else {
        console.error('order为空，创建订单可能失败');
      }
    } catch (error) {
      toast.error('创建订单失败');
    } finally {
      setIsSubmitting(false);
    }
  };
  
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
          <h1 className="flex-1 text-center font-semibold">确认订单</h1>
          <div className="w-16" />
        </div>
      </header>
      
      <div className="max-w-3xl mx-auto px-4 py-4 space-y-4">
        {/* Merchant Info */}
        <Card className="p-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-xl bg-orange-100 flex items-center justify-center">
              <ShoppingBag className="w-6 h-6 text-orange-500" />
            </div>
            <div>
              <h3 className="font-semibold">{merchantName}</h3>
              <p className="text-sm text-gray-500 mt-1">共 {checkoutItems.length} 件商品</p>
            </div>
          </div>
        </Card>
        
        {/* Delivery Info */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4 flex items-center gap-2">
            <MapPin className="w-5 h-5 text-orange-500" />
            配送信息
          </h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                配送地址 <span className="text-red-500">*</span>
              </label>
              <Input
                type="text"
                placeholder="请输入配送地址（如：学生公寓3号楼201）"
                value={deliveryAddress}
                onChange={(e) => {
                  if (e.target.value.length <= 100) {
                    setDeliveryAddress(e.target.value);
                  }
                }}
                className="w-full"
                maxLength={100}
              />
              <p className="text-xs text-gray-400 mt-1 text-right">
                {deliveryAddress.length}/100
              </p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                联系电话 <span className="text-red-500">*</span>
              </label>
              <Input
                type="tel"
                placeholder="请输入手机号"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                className="w-full"
                maxLength={11}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                联系人
              </label>
              <Input
                type="text"
                placeholder="请输入联系人姓名（选填）"
                value={contactName}
                onChange={(e) => setContactName(e.target.value)}
                className="w-full"
              />
            </div>
          </div>
        </Card>

        {/* Order Items */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4">商品清单</h3>
          <div className="space-y-3">
            {checkoutItems.map((item, idx) => (
              <div key={idx} className="flex justify-between items-center py-2">
                <div className="flex items-center gap-3">
                  <div className="w-16 h-16 rounded-lg overflow-hidden bg-gray-100">
                    {item.image && (
                      <img 
                        src={item.image} 
                        alt={item.name}
                        className="w-full h-full object-cover"
                      />
                    )}
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{item.name || '未知商品'}</p>
                    <p className="text-sm text-gray-500">x{item.quantity || 0}</p>
                  </div>
                </div>
                <span className="text-gray-900 font-medium">
                  ¥{((item.price || 0) * (item.quantity || 0)).toFixed(2)}
                </span>
              </div>
            ))}
          </div>
          <div className="border-t mt-4 pt-4 space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">商品总价</span>
              <span>¥{totalPrice.toFixed(2)}</span>
            </div>
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">配送费</span>
              <span className="text-green-500">免费</span>
            </div>
            <div className="flex justify-between font-semibold text-lg pt-2 border-t">
              <span>实付金额</span>
              <span className="text-orange-500">¥{totalPrice.toFixed(2)}</span>
            </div>
          </div>
        </Card>
        
        {/* Remark */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4">订单备注</h3>
          <textarea
            value={remark}
            onChange={(e) => {
              if (e.target.value.length <= 200) {
                setRemark(e.target.value);
              }
            }}
            placeholder="请输入备注信息（选填）"
            className="w-full h-24 p-3 border rounded-xl resize-none focus:outline-none focus:ring-2 focus:ring-orange-500"
            maxLength={200}
          />
          <p className="text-xs text-gray-400 mt-2 text-right">
            {remark.length}/200
          </p>
        </Card>
        
        {/* Payment Method */}
        <Card className="p-4">
          <h3 className="font-semibold mb-4">支付方式</h3>
          <div className="space-y-3">
            <div className="flex items-center gap-3 p-3 rounded-xl bg-orange-50 border-2 border-orange-500">
              <Wallet className="w-5 h-5 text-orange-500" />
              <span className="font-medium">模拟支付</span>
            </div>
          </div>
        </Card>
        
        {/* Submit Button */}
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-4">
          <div className="max-w-3xl mx-auto">
            <Button 
              onClick={handleSubmit}
              disabled={isSubmitting || createOrderLoading}
              className="w-full h-12 rounded-xl gradient-primary text-white font-medium text-lg"
            >
              {isSubmitting || createOrderLoading ? '提交中...' : `提交订单 ¥${totalPrice.toFixed(2)}`}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
