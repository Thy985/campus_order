/**
 * 订单数据Hook
 * 封装订单列表和详情的获取逻辑
 */

import { useState, useEffect, useCallback } from 'react';
import { orderApi } from '@/api';
import { adaptOrder, adaptOrderList, convertOrderStatusToBackend } from '@/utils/dataAdapter';
import type { Order, OrderStatus } from '@/types';

interface UseOrdersOptions {
  page?: number;
  pageSize?: number;
  status?: OrderStatus | 'all';
}

/**
 * 获取订单列表
 */
export function useOrders(options: UseOrdersOptions = {}) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const params: any = {
        page: options.page || 1,
        pageSize: options.pageSize || 10,
      };
      
      // 转换状态值
      if (options.status && options.status !== 'all') {
        params.status = convertOrderStatusToBackend(options.status);
      }
      
      const response = await orderApi.getOrders(params);
      
      // 适配数据
      const adaptedList = response.orderList?.map(adaptOrder) || [];
      setOrders(adaptedList);
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

  return {
    orders,
    loading,
    error,
    total,
    refresh: fetchOrders,
  };
}

/**
 * 获取订单详情
 */
export function useOrderDetail(orderId: string | number | undefined) {
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!orderId) return;
    
    const fetchOrder = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await orderApi.getOrderById(String(orderId));
        setOrder(adaptOrder(response));
      } catch (err) {
        setError(err instanceof Error ? err : new Error('获取订单详情失败'));
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  return { order, loading, error };
}

/**
 * 取消订单
 */
export function useCancelOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const cancelOrder = async (orderId: string | number) => {
    try {
      setLoading(true);
      setError(null);
      await orderApi.cancelOrder(String(orderId));
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
 * 创建订单
 */
export function useCreateOrder() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const createOrder = async (data: {
    merchantId: number;
    items: { productId: number; quantity: number }[];
    remark?: string;
  }) => {
    try {
      setLoading(true);
      setError(null);
      const response = await orderApi.createOrder(data);
      return adaptOrder(response);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('创建订单失败'));
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createOrder, loading, error };
}

