import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CreditCard, AlertCircle, Shield, QrCode, Smartphone, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { paymentApi, orderApi } from '@/api';
import { adaptOrder } from '@/utils/dataAdapter';
import type { Order } from '@/types';
import { OrderStatus } from '@/types';
import { QRCodeSVG } from 'qrcode.react';

type PaymentMode = 'select' | 'pc' | 'qrcode';

export function PaymentPage() {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [order, setOrder] = useState<Order | null>(null);
  const [isLoadingOrder, setIsLoadingOrder] = useState(true);
  const [paymentMode, setPaymentMode] = useState<PaymentMode>('select');
  const [qrCodeUrl, setQrCodeUrl] = useState<string>('');
  const [polling, setPolling] = useState(false);
  const pollTimerRef = useRef<number | null>(null);

  useEffect(() => {
    if (orderId) {
      loadOrder(orderId);
    }
    return () => {
      if (pollTimerRef.current) {
        clearInterval(pollTimerRef.current);
      }
    };
  }, [orderId]);

  const loadOrder = async (id: string) => {
    try {
      setIsLoadingOrder(true);
      const response = await orderApi.getOrderById(id);
      // 使用 adaptOrder 适配后端返回的 OrderDetailResponse 结构
      const data = adaptOrder(response);
      setOrder(data);

      if (data.status !== OrderStatus.PENDING_PAYMENT) {
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

  const handlePcPayment = async () => {
    if (!orderId) return;

    setLoading(true);
    try {
      const formHtml = await paymentApi.createAlipayPayment(Number(orderId));
      const div = document.createElement('div');
      div.innerHTML = formHtml;
      document.body.appendChild(div);
      const form = div.querySelector('form');
      if (form) {
        form.submit();
      } else {
        toast.error('支付表单生成失败');
      }
    } catch (error: any) {
      console.error('支付发起失败:', error);
      const errorMsg = error.response?.data?.message || error.message || '未知错误';
      // 如果是支付宝沙箱错误，提示用户使用模拟支付
      if (errorMsg.includes('Business Failed') || errorMsg.includes('40004')) {
        toast.error('支付宝沙箱支付暂不可用，请使用模拟支付功能');
      } else {
        toast.error('支付发起失败: ' + errorMsg);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleQrCodePayment = async () => {
    if (!orderId) return;

    setLoading(true);
    try {
      const qrUrl = await paymentApi.createAlipayQrCodePayment(Number(orderId));
      setQrCodeUrl(qrUrl);
      setPaymentMode('qrcode');
      startPolling();
    } catch (error: any) {
      console.error('获取支付二维码失败:', error);
      // 如果是支付宝沙箱错误，提示用户使用模拟支付
      if (error.response?.data?.message?.includes('Business Failed') ||
          error.message?.includes('Business Failed')) {
        toast.error('支付宝沙箱扫码支付暂不可用，请使用模拟支付或网页支付');
      } else {
        toast.error('获取支付二维码失败: ' + (error.response?.data?.message || error.message || '未知错误'));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSimulatedPayment = async () => {
    if (!order) return;

    setLoading(true);
    try {
      await paymentApi.simulatedPaySuccess(order.orderNo);
      toast.success('模拟支付成功');
      navigate(`/order/${order.id}`);
    } catch (error: any) {
      console.error('模拟支付失败:', error);
      toast.error('模拟支付失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };

  const startPolling = () => {
    setPolling(true);
    const currentOrderId = orderId;
    const currentOrderNo = order?.orderNo;

    if (!currentOrderId || !currentOrderNo) {
      toast.error('订单信息错误');
      return;
    }

    pollTimerRef.current = setInterval(async () => {
      if (!currentOrderId) return;

      try {
        const status = await paymentApi.queryPaymentStatus(currentOrderNo);
        if (status === 2) {
          if (pollTimerRef.current) {
            clearInterval(pollTimerRef.current);
          }
          toast.success('支付成功');
          navigate(`/order/${currentOrderId}`);
        }
      } catch (error) {
        console.error('查询支付状态失败:', error);
      }
    }, 2000);
  };

  const handleBack = () => {
    if (pollTimerRef.current) {
      clearInterval(pollTimerRef.current);
    }
    setPaymentMode('select');
    setQrCodeUrl('');
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

  if (paymentMode === 'qrcode') {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white sticky top-0 z-10 border-b">
          <div className="max-w-3xl mx-auto px-4 h-14 flex items-center gap-4">
            <button
              onClick={handleBack}
              className="p-2 hover:bg-gray-100 rounded-full transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-lg font-semibold">扫码支付</h1>
          </div>
        </header>

        <div className="max-w-3xl mx-auto px-4 py-6 space-y-6">
          <Card className="p-6">
            <div className="flex items-center justify-between mb-4">
              <span className="text-gray-500">订单编号</span>
              <span className="font-medium">{order.orderNo}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-gray-500">支付金额</span>
              <span className="text-2xl font-bold text-red-500">
                ¥{(order.actualAmount || 0).toFixed(2)}
              </span>
            </div>
          </Card>

          <Card className="p-6">
            <h2 className="text-lg font-semibold mb-4 text-center">请使用支付宝扫码支付</h2>
            <div className="flex justify-center mb-4">
              {qrCodeUrl && (
                <QRCodeSVG
                  value={qrCodeUrl}
                  size={192}
                  level="H"
                  includeMargin={true}
                  className="border rounded-lg"
                />
              )}
            </div>
            <div className="text-center text-sm text-gray-500">
              {polling ? (
                <div className="flex items-center justify-center gap-2">
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>等待支付中...</span>
                </div>
              ) : (
                <span>二维码已失效</span>
              )}
            </div>
          </Card>

          <div className="text-center text-sm text-gray-500">
            <p>打开手机支付宝，扫描上方二维码完成支付</p>
            <p className="mt-1">测试账号：olvgts1730@sandbox.com | 支付密码：111111</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
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
        <Card className="p-6">
          <div className="flex items-center justify-between mb-4">
            <span className="text-gray-500">订单编号</span>
            <span className="font-medium">{order.orderNo}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-500">支付金额</span>
            <span className="text-2xl font-bold text-red-500">
              ¥{(order.actualAmount || 0).toFixed(2)}
            </span>
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-lg font-semibold mb-4">选择支付方式</h2>
          
          <div 
            className="p-4 border-2 border-blue-500 bg-blue-50 rounded-xl flex items-center gap-4 cursor-pointer hover:bg-blue-100 transition-colors"
            onClick={handlePcPayment}
          >
            <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-white" />
            </div>
            <div className="flex-1">
              <div className="font-medium">支付宝网页支付</div>
              <div className="text-sm text-gray-500">在网页上完成支付</div>
            </div>
          </div>

          <div 
            className="mt-3 p-4 border-2 border-green-500 bg-green-50 rounded-xl flex items-center gap-4 cursor-pointer hover:bg-green-100 transition-colors"
            onClick={handleQrCodePayment}
          >
            <div className="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center">
              <Smartphone className="w-5 h-5 text-white" />
            </div>
            <div className="flex-1">
              <div className="font-medium">支付宝扫码支付</div>
              <div className="text-sm text-gray-500">使用手机支付宝APP扫码支付</div>
            </div>
            <QrCode className="w-5 h-5 text-green-600" />
          </div>

          {/* 模拟支付按钮 - 仅用于测试 */}
          <div 
            className="mt-3 p-4 border-2 border-purple-500 bg-purple-50 rounded-xl flex items-center gap-4 cursor-pointer hover:bg-purple-100 transition-colors"
            onClick={handleSimulatedPayment}
          >
            <div className="w-10 h-10 bg-purple-500 rounded-full flex items-center justify-center">
              <Shield className="w-5 h-5 text-white" />
            </div>
            <div className="flex-1">
              <div className="font-medium">模拟支付（测试用）</div>
              <div className="text-sm text-gray-500">立即模拟支付成功，无需真实付款</div>
            </div>
          </div>
        </Card>

        <div className="text-center text-sm text-gray-500">
          <p>支付安全由支付宝沙箱环境保障</p>
          <p className="mt-1">测试账号：olvgts1730@sandbox.com | 登录密码：111111 | 支付密码：111111</p>
        </div>
      </div>
    </div>
  );
}
