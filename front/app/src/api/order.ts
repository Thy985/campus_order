import { get, post } from '@/lib/request';
import type { Order, CreateOrderParams } from '@/types';

/**
 * 获取订单列表
 * 修改为与后端对齐：GET /api/order/list
 */
export async function getOrders(params?: {
  page?: number;
  pageSize?: number;
  status?: number;
}): Promise<{
  total: number;
  orderList: Order[];
  page: number;
  pageSize: number;
  totalPages: number;
}> {
  return get<{
    total: number;
    orderList: Order[];
    page: number;
    pageSize: number;
    totalPages: number;
  }>('/api/order/list', { params });
}

/**
 * 获取订单详情
 * 修改为与后端对齐：GET /api/order/{orderId}
 */
export async function getOrderById(id: string | number): Promise<Order> {
  return get<Order>(`/api/order/${id}`);
}

/**
 * 创建订单
 * 修改为与后端对齐：POST /api/order
 */
export async function createOrder(data: CreateOrderParams): Promise<Order> {
  return post<Order>('/api/order', data);
}

/**
 * 取消订单
 * 注意：后端需要实现此接口 POST /api/order/{orderId}/cancel
 */
export async function cancelOrder(id: string | number): Promise<void> {
  return post<void>(`/api/order/${id}/cancel`);
}

/**
 * 支付订单
 * 注意：后端需要实现此接口 POST /api/order/{orderId}/pay
 * 或使用 /api/payment/create
 */
export async function payOrder(id: string | number): Promise<void> {
  // 暂时使用支付接口
  return post<void>('/api/payment/create', { orderNo: id });
}

/**
 * 确认取餐
 * 注意：后端需要实现此接口
 */
export async function confirmPickup(id: string | number): Promise<void> {
  return post<void>(`/api/order/${id}/confirm-pickup`);
}

/**
 * 根据订单号获取订单
 */
export async function getOrderByOrderNo(orderNo: string): Promise<Order> {
  return get<Order>(`/api/order/by-no/${orderNo}`);
}

