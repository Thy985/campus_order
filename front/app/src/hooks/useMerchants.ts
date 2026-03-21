/**
 * 商家数据Hook
 * 封装商家列表和详情的获取逻辑
 */

import { useState, useEffect, useCallback } from 'react';
import { merchantApi } from '@/api';
import { adaptMerchant, adaptMerchantList, adaptPaginatedResponse } from '@/utils/dataAdapter';
import type { Store } from '@/types';

interface UseMerchantsOptions {
  page?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
}

/**
 * 获取商家列表
 */
export function useMerchants(options: UseMerchantsOptions = {}) {
  const [merchants, setMerchants] = useState<Store[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(options.page || 1);

  const fetchMerchants = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await merchantApi.getMerchantList({
        page: options.page || 1,
        pageSize: options.pageSize || 10,
        categoryId: options.categoryId,
        keyword: options.keyword,
      });
      
      // 使用适配器处理分页数据
      const adapted = adaptPaginatedResponse(response, adaptMerchant);
      setMerchants(adapted.list);
      setTotal(adapted.total);
      setPage(adapted.page);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取商家列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.categoryId, options.keyword]);

  useEffect(() => {
    fetchMerchants();
  }, [fetchMerchants]);

  return {
    merchants,
    loading,
    error,
    total,
    page,
    refresh: fetchMerchants,
  };
}

/**
 * 获取商家详情
 */
export function useMerchantDetail(merchantId: number | string | undefined) {
  const [merchant, setMerchant] = useState<Store | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!merchantId) return;
    
    const fetchMerchant = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await merchantApi.getMerchantDetail(Number(merchantId));
        setMerchant(adaptMerchant(response));
      } catch (err) {
        setError(err instanceof Error ? err : new Error('获取商家详情失败'));
      } finally {
        setLoading(false);
      }
    };

    fetchMerchant();
  }, [merchantId]);

  return { merchant, loading, error };
}

