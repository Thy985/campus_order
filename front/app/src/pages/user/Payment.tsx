import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CreditCard, AlertCircle, Shield } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { paymentApi, orderApi } from '@/api';
import type { Order } from '@/types';

export function PaymentPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoadingOrder, setIsLoadingOrder] = useState(true);

  useEffect(() => {
    if (orderId) {
      loadOrder(Number(orderId));
    }
  }, [orderId]);

  const loadOrder = async (id: number) => {
    try {
      setIsLoadingOrder(true);
      const data = await orderApi.getOrderById(id);
      setOrder(data);
      
      // 如果订单已支付，跳转到订单详情
      if (data.status !== 1) { // 1 = 待支付
        toast.info('该订单已支付或已取消');
        navigate(`/order/${id}`);
      }
    } catch (error) {
      console.error('加载订单失败:', error);
      toast.error('加载订单失败');
    } finally {
      setIsLoadingOrder(false);
    }
  };

  const handlePayment = async () => {
    if (!orderId) return;
    
    setLoading(true);
    try {
      // 支付宝沙箱支付
      const formHtml = await paymentApi.createAlipayPayment(Number(orderId));
      // 创建临时表单提交
      const div = document.createElement('div');
      div.innerHTML = formHtml;
      document.body.appendChild(div);
      const form = div.querySelector('form');
      if (form) {
        form.submit();
      } else {
        toast.error('支付表单生成失败');
      }
    } catch (error) {
      console.error('支付发起失败:', error);
      toast.error('支付发起失败');
    } finally {
      setLoading(false);
    }
  };

  if (isLoadingOrder) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
          <h2 className="text-lg font-medium text-gray-900">订单不存在</h2>
          <Button onClick={() => navigate('/orders')} className="mt-4">
            返回订单列表
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white sticky top-0 z-10 border-b">
        <div className="max-w-3xl mx-auto px-4 h-14 flex items-center gap-4">
          <button
            onClick={() => navigate(-1)}
            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-lg font-semibold">确认支付</h1>
        </div>
      </header>

      <div className="max-w-3xl mx-auto px-4 py-6 space-y-6">
        {/* 订单信息 */}
        <Card className="p-6">
          <div className="flex items-center justify-between mb-4">
            <span className="text-gray-500">订单编号</span>
            <span className="font-medium">{order.orderNo}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">支付金额</span>
            <span className="text-2xl font-bold text-red-500">
              ¥{order.actualAmount.toFixed(2)}
            </span>
          </div>
        </Card>

        {/* 支付方式 */}
        <Card className="p-6">
          <h2 className="text-lg font-semibold mb-4">支付方式</h2>
          
          <div className="p-4 border-2 border-blue-500 bg-blue-50 rounded-xl flex items-center gap-4">
            <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-white" />
            </div>
            <div className="flex-1">
              <div className="font-medium">支付宝沙箱支付</div>
              <div className="text-sm text-gray-500">安全便捷的在线支付方式</div>
            </div>
            <div className="flex items-center gap-1 text-blue-600 text-sm">
              <Shield className="w-4 h-4" />
              <span>安全</span>
            </div>
          </div>
        </Card>

        {/* 支付按钮 */}
        <Button
          onClick={handlePayment}
          disabled={loading}
          className="w-full h-14 text-lg font-medium rounded-xl bg-blue-500 hover:bg-blue-600"
        >
          {loading ? '支付中...' : `确认支付 ¥${order.actualAmount.toFixed(2)}`}
        </Button>

        {/* 安全提示 */}
        <div className="text-center text-sm text-gray-500">
          <p>支付安全由支付宝沙箱环境保障</p>
          <p className="mt-1">测试账号：sandbox@alipay.com | 密码：111111</p>
        </div>
      </div>
    </div>
  );
}
