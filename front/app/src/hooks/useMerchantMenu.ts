/**
 * 商家菜品管理Hook
 * 封装商家菜品CRUD操作
 */

import { useState, useEffect, useCallback } from 'react';
import { merchantApi } from '@/api';
import { adaptProduct, adaptProductList } from '@/utils/dataAdapter';
import type { Dish } from '@/types';

interface UseMerchantMenuOptions {
  merchantId: number;
  page?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
}

/**
 * 获取商家菜品列表
 */
export function useMerchantMenu(options: UseMerchantMenuOptions) {
  const [dishes, setDishes] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchDishes = useCallback(async () => {
    if (!options.merchantId) return;
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await merchantApi.getMerchantProducts({
        page: options.page || 1,
        pageSize: options.pageSize || 20,
        keyword: options.keyword,
      });
      
      const adaptedList = response.productList?.map(adaptProduct) || [];
      setDishes(adaptedList);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取菜品列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.page, options.pageSize, options.keyword]);

  useEffect(() => {
    fetchDishes();
  }, [fetchDishes]);

  return {
    dishes,
    loading,
    error,
    total,
    refresh: fetchDishes,
  };
}

/**
 * 创建菜品
 */
export function useCreateDish() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const createDish = async (data: {
    name: string;
    description?: string;
    price: number;
    originalPrice?: number;
    stock: number;
    image?: string;
    category?: string;
    status?: number;
  }): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      await merchantApi.createProduct({
        name: data.name,
        description: data.description,
        price: data.price,
        originalPrice: data.originalPrice,
        stock: data.stock,
        image: data.image,
        category: data.category,
        status: data.status ?? 1,
      });
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('创建菜品失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { createDish, loading, error };
}

/**
 * 更新菜品
 */
export function useUpdateDish() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const updateDish = async (
    dishId: number,
    data: Partial<{
      name: string;
      description: string;
      price: number;
      originalPrice: number;
      stock: number;
      image: string;
      category: string;
      status: number;
    }>
  ): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      await merchantApi.updateProduct(dishId, data);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('更新菜品失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { updateDish, loading, error };
}

/**
 * 删除菜品
 */
export function useDeleteDish() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const deleteDish = async (dishId: number): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      await merchantApi.deleteProduct(dishId);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('删除菜品失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { deleteDish, loading, error };
}

/**
 * 切换菜品上下架状态
 */
export function useToggleDishAvailability() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const toggleAvailability = async (
    dishId: number,
    currentStatus: boolean
  ): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      if (currentStatus) {
        await merchantApi.unpublishProduct(dishId);
      } else {
        await merchantApi.publishProduct(dishId);
      }
      return true;
    } catch (err) {
      setError(err instanceof Error ? err : new Error('切换菜品状态失败'));
      return false;
    } finally {
      setLoading(false);
    }
  };

  return { toggleAvailability, loading, error };
}
