/**
 * 管理员数据Hook
 * 封装管理后台数据的获取逻辑
 */

import { useState, useEffect, useCallback } from 'react';
import { adminApi } from '@/api';
import { adaptUser, adaptOrder, adaptMerchant } from '@/utils/dataAdapter';
import type { User, Order } from '@/types';
import type { AdminMerchant } from '@/api/admin';

interface AdminStats {
  totalUsers: number;
  totalMerchants: number;
  totalProducts: number;
  totalOrders: number;
  todayOrders: number;
  pendingOrders: number;
  todaySales: number;
  totalSales: number;
}

/**
 * 获取管理员统计数据
 */
export function useAdminStats() {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const overview = await adminApi.getDashboardOverview();
      
      setStats({
        totalUsers: overview.totalUsers,
        totalMerchants: overview.totalMerchants,
        totalProducts: overview.totalProducts,
        totalOrders: overview.totalOrders,
        todayOrders: overview.todayOrders,
        pendingOrders: overview.pendingOrders,
        todaySales: overview.todaySales,
        totalSales: overview.totalSales,
      });
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取统计数据失败'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, loading, error, refresh: fetchStats };
}

/**
 * 获取用户列表（管理员）
 */
export function useAdminUsers(options: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
} = {}) {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 调用新的管理端用户接口
      const response = await adminApi.getAdminUsers({
        page: options.page || 1,
        pageSize: options.pageSize || 10,
        keyword: options.keyword,
        status: options.status,
      });
      
      const adaptedUsers = response.list?.map(adaptUser) || [];
      setUsers(adaptedUsers);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取用户列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.keyword, options.status]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  return { users, loading, error, total, refresh: fetchUsers };
}

/**
 * 获取商家列表（管理员）
 */
export function useAdminMerchants(options: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
  categoryId?: number;
} = {}) {
  const [merchants, setMerchants] = useState<AdminMerchant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchMerchants = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 调用管理端商家列表接口
      const response = await adminApi.getAdminMerchants({
        page: options.page || 1,
        pageSize: options.pageSize || 10,
        keyword: options.keyword,
        status: options.status,
        categoryId: options.categoryId,
      });
      
      setMerchants(response.merchantList || []);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取商家列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.keyword, options.status, options.categoryId]);

  useEffect(() => {
    fetchMerchants();
  }, [fetchMerchants]);

  return { merchants, loading, error, total, refresh: fetchMerchants };
}

/**
 * 获取商家详情（管理员）
 */
export function useAdminMerchantDetail(merchantId: number | null) {
  const [merchant, setMerchant] = useState<AdminMerchant | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchMerchantDetail = useCallback(async () => {
    if (!merchantId) return;
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await adminApi.getAdminMerchantDetail(merchantId);
      setMerchant(response);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取商家详情失败'));
    } finally {
      setLoading(false);
    }
  }, [merchantId]);

  useEffect(() => {
    fetchMerchantDetail();
  }, [fetchMerchantDetail]);

  return { merchant, loading, error, refresh: fetchMerchantDetail };
}

/**
 * 创建商家
 */
export function useCreateMerchant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const createMerchant = async (data: Parameters<typeof adminApi.createMerchant>[0]) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminApi.createMerchant(data);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('创建商家失败'));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createMerchant, loading, error };
}

/**
 * 更新商家
 */
export function useUpdateMerchant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const updateMerchant = async (merchantId: number, data: Parameters<typeof adminApi.updateMerchant>[1]) => {
    try {
      setLoading(true);
      setError(null);
      const response = await adminApi.updateMerchant(merchantId, data);
      return response;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('更新商家失败'));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { updateMerchant, loading, error };
}

/**
 * 删除商家
 */
export function useDeleteMerchant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const deleteMerchant = async (merchantId: number) => {
    try {
      setLoading(true);
      setError(null);
      await adminApi.deleteMerchant(merchantId);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('删除商家失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { deleteMerchant, loading, error };
}

/**
 * 获取所有订单（管理员）
 */
export function useAdminOrders(options: {
  page?: number;
  pageSize?: number;
  status?: number;
  merchantId?: number;
  userId?: number;
} = {}) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 调用新的管理端订单接口
      const response = await adminApi.getAdminOrders({
        page: options.page || 1,
        pageSize: options.pageSize || 10,
        status: options.status,
        merchantId: options.merchantId,
        userId: options.userId,
      });
      
      const adaptedOrders = response.orderList?.map((order: any) => adaptOrder({
        ...order,
        id: String(order.id),
        status: order.status,
      })) || [];
      
      setOrders(adaptedOrders);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取订单列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.status, options.merchantId, options.userId]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  return { orders, loading, error, total, refresh: fetchOrders };
}

/**
 * 禁用/启用用户
 */
export function useToggleUserStatus() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const toggleStatus = async (userId: number, status: number) => {
    try {
      setLoading(true);
      setError(null);
      await adminApi.updateUserStatus(userId, status);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('修改用户状态失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { toggleStatus, loading, error };
}

/**
 * 审核商家
 */
export function useAuditMerchant() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const audit = async (merchantId: number, status: number, reason?: string) => {
    try {
      setLoading(true);
      setError(null);
      await adminApi.auditMerchant(merchantId, status, reason);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('审核商家失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { audit, loading, error };
}

/**
 * 管理员取消订单
 */
export function useAdminCancelOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const cancelOrder = async (orderId: number, reason?: string) => {
    try {
      setLoading(true);
      setError(null);
      await adminApi.cancelOrderByAdmin(orderId, reason);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('取消订单失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { cancelOrder, loading, error };
}

/**
 * 获取商家统计
 */
export function useMerchantStatistics() {
  const [statistics, setStatistics] = useState<{
    totalMerchants: number;
    pendingAudit: number;
    activeMerchants: number;
    inactiveMerchants: number;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchStatistics = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await adminApi.getMerchantStatistics();
      setStatistics(response);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取商家统计失败'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStatistics();
  }, [fetchStatistics]);

  return { statistics, loading, error, refresh: fetchStatistics };
}
