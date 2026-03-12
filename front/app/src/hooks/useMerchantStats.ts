/**
 * 商家统计数据Hook
 * 封装商家后台数据统计的获取逻辑
 */

import { useState, useEffect, useCallback } from 'react';
import { merchantApi } from '@/api';
import { adaptOrder } from '@/utils/dataAdapter';
import type { Order } from '@/types';

interface MerchantStats {
  todayOrders: number;
  todayRevenue: number;
  totalOrders: number;
  totalRevenue: number;
  rating: number;
  pendingOrders: number;
  weeklySales: { day: string; salesAmount: number }[];
  topDishes: { name: string; salesAmount: number }[];
}

/**
 * 获取商家统计数据
 */
export function useMerchantStats(merchantId?: number) {
  const [stats, setStats] = useState<MerchantStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 调用新的商家统计接口
      const data = await merchantApi.getMerchantStatistics();
      setStats(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取统计数据失败'));
    } finally {
      setLoading(false);
    }
  }, [merchantId]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, loading, error, refresh: fetchStats };
}

/**
 * 获取商家订单列表（商家视角）
 */
export function useMerchantOrders(options: {
  page?: number;
  pageSize?: number;
  status?: number;
} = {}) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 调用新的商家订单接口
      const response = await merchantApi.getMerchantOrders({
        page: options.page || 1,
        pageSize: options.pageSize || 10,
        status: options.status,
      });
      
      const adaptedOrders = response.orderList?.map((order: any) => adaptOrder({
        ...order,
        merchantName: '本店', // 商家端不需要显示商家名
      })) || [];
      
      setOrders(adaptedOrders);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取订单列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.status]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  return { orders, loading, error, total, refresh: fetchOrders };
}

/**
 * 商家接单
 */
export function useAcceptOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const acceptOrder = async (orderId: string | number) => {
    try {
      setLoading(true);
      setError(null);
      await merchantApi.acceptOrder(Number(orderId));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('接单失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { acceptOrder, loading, error };
}

/**
 * 商家拒绝订单
 */
export function useRejectOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const rejectOrder = async (orderId: string | number, reason?: string) => {
    try {
      setLoading(true);
      setError(null);
      await merchantApi.rejectOrder(Number(orderId), reason);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('拒单失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { rejectOrder, loading, error };
}

/**
 * 商家完成订单
 */
export function useCompleteOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const completeOrder = async (orderId: string | number) => {
    try {
      setLoading(true);
      setError(null);
      await merchantApi.completeOrder(Number(orderId));
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('完成订单失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { completeOrder, loading, error };
}

