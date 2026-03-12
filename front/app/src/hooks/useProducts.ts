/**
 * 商品数据Hook
 * 封装商品列表和详情的获取逻辑
 */

import { useState, useEffect, useCallback } from 'react';
import { productApi } from '@/api';
import { adaptProduct } from '@/utils/dataAdapter';
import type { Dish } from '@/types';

interface UseProductsOptions {
  merchantId: number;
  categoryId?: number;
  page?: number;
  pageSize?: number;
  keyword?: string;
}

/**
 * 获取商品列表
 */
export function useProducts(options: UseProductsOptions) {
  const [products, setProducts] = useState<Dish[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [total, setTotal] = useState(0);

  const fetchProducts = useCallback(async () => {
    if (!options.merchantId) return;
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await productApi.getProductList({
        merchantId: options.merchantId,
        categoryId: options.categoryId,
        page: options.page || 1,
        pageSize: options.pageSize || 20,
        keyword: options.keyword,
      });
      
      // 适配数据
      const adaptedList = response.productList?.map(adaptProduct) || [];
      setProducts(adaptedList);
      setTotal(response.total || 0);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('获取商品列表失败'));
    } finally {
      setLoading(false);
    }
  }, [options.merchantId, options.categoryId, options.page, options.pageSize, options.keyword]);

  useEffect(() => {
    fetchProducts();
  }, [fetchProducts]);

  return {
    products,
    loading,
    error,
    total,
    refresh: fetchProducts,
  };
}

/**
 * 获取商品详情
 */
export function useProductDetail(productId: number | undefined) {
  const [product, setProduct] = useState<Dish | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!productId) return;
    
    const fetchProduct = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await productApi.getProductDetail(productId);
        setProduct(adaptProduct(response));
      } catch (err) {
        setError(err instanceof Error ? err : new Error('获取商品详情失败'));
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [productId]);

  return { product, loading, error };
}

/**
 * 获取商品分类
 */
export function useProductCategories(merchantId?: number) {
  const [categories, setCategories] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const response = await productApi.getProductCategories(merchantId);
        setCategories(response || []);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('获取商品分类失败'));
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, [merchantId]);

  return { categories, loading, error };
}

