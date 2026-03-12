import { get, put, del, post } from '@/lib/request';
import type { User } from '@/types';

// ==================== Dashboard 接口 ====================

export interface DashboardOverview {
  totalUsers: number;
  totalMerchants: number;
  totalProducts: number;
  totalOrders: number;
  todayOrders: number;
  pendingOrders: number;
  todaySales: number;
  totalSales: number;
}

export interface OrderStatistics {
  waitPay: number;
  waitAccept: number;
  making: number;
  waitPickup: number;
  completed: number;
  cancelled: number;
  total: number;
}

/**
 * 获取Dashboard概览数据
 * GET /api/admin/dashboard/overview
 */
export async function getDashboardOverview(): Promise<DashboardOverview> {
  return get('/api/admin/dashboard/overview');
}

/**
 * 获取订单统计
 * GET /api/admin/dashboard/order-statistics
 */
export async function getOrderStatisticsDetail(): Promise<OrderStatistics> {
  return get('/api/admin/dashboard/order-statistics');
}

/**
 * 获取实时订单
 * GET /api/admin/dashboard/realtime-orders
 */
export async function getRealtimeOrders(limit: number = 20): Promise<any[]> {
  return get('/api/admin/dashboard/realtime-orders', { params: { limit } });
}

// ==================== 用户管理接口 ====================

export interface AdminUserListResponse {
  total: number;
  page: number;
  pageSize: number;
  list: User[];
}

/**
 * 获取用户列表
 * GET /api/admin/users
 */
export async function getAdminUsers(params: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
}): Promise<AdminUserListResponse> {
  return get('/api/admin/users', { params });
}

/**
 * 修改用户状态
 * PUT /api/admin/users/{userId}/status
 */
export async function updateUserStatus(userId: number, status: number): Promise<void> {
  return put(`/api/admin/users/${userId}/status`, null, { params: { status } });
}

/**
 * 获取用户详情
 * GET /api/admin/users/{userId}
 */
export async function getUserDetail(userId: number): Promise<User> {
  return get(`/api/admin/users/${userId}`);
}

/**
 * 删除用户
 * DELETE /api/admin/users/{userId}
 */
export async function deleteUser(userId: number): Promise<void> {
  return del(`/api/admin/users/${userId}`);
}

// ==================== 订单管理接口 ====================

export interface AdminOrder {
  id: number;
  orderNo: string;
  merchantId: number;
  merchantName: string;
  userId: number;
  status: number;
  totalAmount: number;
  createTime: string;
}

export interface AdminOrderListResponse {
  total: number;
  page: number;
  pageSize: number;
  orderList: AdminOrder[];
}

/**
 * 获取订单列表
 * GET /api/admin/orders
 */
export async function getAdminOrders(params: {
  page?: number;
  pageSize?: number;
  status?: number;
  merchantId?: number;
  userId?: number;
}): Promise<AdminOrderListResponse> {
  return get('/api/admin/orders', { params });
}

/**
 * 获取订单详情
 * GET /api/admin/orders/{orderId}
 */
export async function getAdminOrderDetail(orderId: number): Promise<AdminOrder> {
  return get(`/api/admin/orders/${orderId}`);
}

/**
 * 取消订单
 * POST /api/admin/orders/{orderId}/cancel
 */
export async function cancelOrderByAdmin(orderId: number, reason?: string): Promise<void> {
  return post(`/api/admin/orders/${orderId}/cancel`, null, { params: { reason } });
}

/**
 * 获取订单统计
 * GET /api/admin/orders/statistics
 */
export async function getOrderStatistics(): Promise<{
  totalOrders: number;
  todayOrders: number;
  pendingOrders: number;
  totalRevenue: number;
}> {
  return get('/api/admin/orders/statistics');
}

// ==================== 商家管理接口 ====================

export interface AdminMerchant {
  id: number;
  name: string;
  logo: string;
  phone: string;
  address: string;
  description?: string;
  categoryId?: number;
  categoryName?: string;
  rating: number;
  salesVolume: number;
  minPrice: number;
  status: number;
  createTime: string;
  updateTime?: string;
}

export interface AdminMerchantListResponse {
  total: number;
  page: number;
  pageSize: number;
  merchantList: AdminMerchant[];
}

export interface CreateMerchantRequest {
  name: string;
  phone: string;
  address: string;
  description?: string;
  categoryId?: number;
  minPrice?: number;
  logo?: string;
}

export interface UpdateMerchantRequest {
  name?: string;
  phone?: string;
  address?: string;
  description?: string;
  categoryId?: number;
  minPrice?: number;
  logo?: string;
  status?: number;
}

/**
 * 获取商家列表
 * GET /api/admin/merchants
 */
export async function getAdminMerchants(params: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
  categoryId?: number;
}): Promise<AdminMerchantListResponse> {
  return get('/api/admin/merchants', { params });
}

/**
 * 获取商家详情
 * GET /api/admin/merchants/{merchantId}
 */
export async function getAdminMerchantDetail(merchantId: number): Promise<AdminMerchant> {
  return get(`/api/admin/merchants/${merchantId}`);
}

/**
 * 创建商家
 * POST /api/admin/merchants
 */
export async function createMerchant(data: CreateMerchantRequest): Promise<AdminMerchant> {
  return post('/api/admin/merchants', data);
}

/**
 * 更新商家信息
 * PUT /api/admin/merchants/{merchantId}
 */
export async function updateMerchant(merchantId: number, data: UpdateMerchantRequest): Promise<AdminMerchant> {
  return put(`/api/admin/merchants/${merchantId}`, data);
}

/**
 * 审核商家
 * PUT /api/admin/merchants/{merchantId}/audit
 */
export async function auditMerchant(merchantId: number, status: number, reason?: string): Promise<void> {
  return put(`/api/admin/merchants/${merchantId}/audit`, null, { params: { status, reason } });
}

/**
 * 删除商家
 * DELETE /api/admin/merchants/{merchantId}
 */
export async function deleteMerchant(merchantId: number): Promise<void> {
  return del(`/api/admin/merchants/${merchantId}`);
}

/**
 * 获取商家统计
 * GET /api/admin/merchants/statistics
 */
export async function getMerchantStatistics(): Promise<{
  totalMerchants: number;
  pendingAudit: number;
  activeMerchants: number;
  inactiveMerchants: number;
}> {
  return get('/api/admin/merchants/statistics');
}

