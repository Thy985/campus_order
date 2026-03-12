import { get, post } from '@/lib/request';

export interface PaymentResponse {
  paymentNo: string;
  payUrl?: string;
  payFormHtml?: string;
}

export interface WechatPayParams {
  appId?: string;
  timeStamp?: string;
  nonceStr?: string;
  package?: string;
  signType?: string;
  paySign?: string;
  h5Url?: string;
  codeUrl?: string;
}

/**
 * 创建支付宝支付
 * 与后端对齐：POST /api/payment/create
 */
export async function createPayment(data: {
  orderNo: string;
  amount?: number;
  description?: string;
}): Promise<PaymentResponse> {
  return post('/api/payment/create', data);
}

/**
 * 查询支付状态
 * 与后端对齐：GET /api/payment/status/{orderNo}
 */
export async function queryPaymentStatus(orderNo: string): Promise<number> {
  return get(`/api/payment/status/${orderNo}`);
}

/**
 * 获取支付记录
 * 与后端对齐：GET /api/payment/record/{orderNo}
 */
export async function getPaymentRecord(orderNo: string): Promise<{
  paymentNo: string;
  orderNo: string;
  amount: number;
  status: number;
  payTime?: string;
}> {
  return get(`/api/payment/record/${orderNo}`);
}

// ==================== 模拟支付接口（仅用于测试）====================

/**
 * 模拟支付成功
 */
export async function simulatedPaySuccess(orderNo: string): Promise<void> {
  return post(`/api/payment/simulated/pay-success/${orderNo}`);
}

/**
 * 创建支付并立即成功（模拟）
 */
export async function createAndPay(orderNo: string): Promise<void> {
  return post(`/api/payment/simulated/create-and-pay/${orderNo}`);
}

/**
 * 模拟支付失败
 */
export async function simulatedPayFail(orderNo: string, reason?: string): Promise<void> {
  return post(`/api/payment/simulated/pay-fail/${orderNo}`, { reason });
}

/**
 * 查询模拟支付状态
 */
export async function querySimulatedPayStatus(orderNo: string): Promise<number> {
  return get(`/api/payment/simulated/status/${orderNo}`);
}

// ==================== 支付宝支付接口 ====================

/**
 * 创建支付宝支付
 * POST /api/payment/alipay/create
 */
export async function createAlipayPayment(orderId: number): Promise<string> {
  return post<string>('/api/payment/alipay/create', { orderId });
}

/**
 * 支付宝支付同步回调
 * GET /api/payment/alipay/return
 */
export async function alipayReturn(params: {
  out_trade_no: string;
  trade_no: string;
  trade_status: string;
}): Promise<void> {
  return get('/api/payment/alipay/return', { params });
}

// ==================== 微信支付接口 ====================

/**
 * 创建微信支付
 * POST /api/payment/wechat/create
 */
export async function createWechatPayment(orderId: number): Promise<WechatPayParams> {
  return post<WechatPayParams>('/api/payment/wechat/create', { orderId });
}

/**
 * 微信支付回调（前端轮询查询支付状态）
 * GET /api/payment/status/{orderNo}
 */
export async function queryWechatPaymentStatus(orderNo: string): Promise<number> {
  return get(`/api/payment/status/${orderNo}`);
}

