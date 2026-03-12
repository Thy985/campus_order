import { get } from '@/lib/request';
import type { Product, ProductDetailResponse } from '@/types';

export interface ProductListRequest {
  merchantId: number;
  categoryId?: number;
  keyword?: string;
  page?: number;
  pageSize?: number;
  status?: number;
  minPrice?: number;
  maxPrice?: number;
}

export interface ProductListResponse {
  total: number;
  productList: Product[];
  page: number;
  pageSize: number;
}

export interface Category {
  id: number;
  name: string;
  merchantId?: number;
  sort?: number;
}

/**
 * 获取商品列表
 * 与后端对齐：GET /api/product/list
 */
export const getProductList = (
  params: ProductListRequest
): Promise<ProductListResponse> => {
  return get('/api/product/list', { params });
};

/**
 * 获取商品详情
 * 与后端对齐：GET /api/product/detail/{id}
 */
export const getProductDetail = (
  id: number
): Promise<ProductDetailResponse> => {
  return get(`/api/product/detail/${id}`);
};

/**
 * 获取商品分类列表
 * 注意：后端需要实现此接口 GET /api/product/category/list
 */
export const getProductCategories = (
  merchantId?: number
): Promise<Category[]> => {
  return get('/api/product/category/list', { params: { merchantId } });
};

export default {
  getProductList,
  getProductDetail,
  getProductCategories,
};

