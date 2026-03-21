/**
 * 数据适配层
 * 将后端数据格式转换为前端使用的格式
 * 统一处理字段名和类型差异
 */

import type { Store, Dish, Order, User } from '@/types';
import { OrderStatus } from '@/types';

// ==================== 商家数据适配 ====================

// 默认图片
const DEFAULT_FOOD_IMAGE = 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&h=300&fit=crop';
const DEFAULT_STORE_IMAGE = 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200&h=200&fit=crop';

/**
 * 将后端商家数据转换为前端Store格式
 */
export function adaptMerchant(backendData: any): Store {
  if (!backendData) return null as any;
  
  // 处理嵌套结构：后端返回 { merchant: {...}, category: {...} }
  const merchant = backendData.merchant || backendData;
  
  return {
    id: Number(merchant.id),
    name: merchant.name,
    logo: merchant.logo || merchant.banner || DEFAULT_STORE_IMAGE,
    rating: Number(merchant.rating) || 4.5,
    monthlySales: Number(merchant.salesVolume) || 0,
    deliveryTime: Number(merchant.deliveryTime) || 30,
    distance: Number(merchant.distance) || 0,
    minPrice: Number(merchant.minOrderAmount) || Number(merchant.minPrice) || 0,
    tags: merchant.tags || [],
    categories: merchant.categories || [],
    banner: merchant.banner || merchant.logo || DEFAULT_STORE_IMAGE,
    address: merchant.address,
    description: merchant.description,
  };
}

/**
 * 批量适配商家列表
 */
export function adaptMerchantList(backendList: any[]): Store[] {
  if (!Array.isArray(backendList)) return [];
  return backendList.map(adaptMerchant);
}

// ==================== 商品数据适配 ====================

/**
 * 将后端商品数据转换为前端Dish格式
 */
export function adaptProduct(backendData: any): Dish {
  if (!backendData) return null as any;
  
  return {
    id: Number(backendData.id),
    storeId: Number(backendData.merchantId),
    name: backendData.name,
    description: backendData.description || '',
    price: Number(backendData.price) || 0,
    image: backendData.image || DEFAULT_FOOD_IMAGE,
    category: String(backendData.categoryId || backendData.category || ''),
    isRecommended: Boolean(backendData.isRecommended),
    isSpicy: Boolean(backendData.isSpicy),
    sales: Number(backendData.salesVolume) || 0,
    originalPrice: Number(backendData.originalPrice) || Number(backendData.price) || 0,
    tags: backendData.tags || [],
    isAvailable: backendData.status === 1,
  };
}

/**
 * 批量适配商品列表
 */
export function adaptProductList(backendList: any[]): Dish[] {
  if (!Array.isArray(backendList)) return [];
  return backendList.map(adaptProduct);
}

// ==================== 订单数据适配 ====================

/**
 * 将后端订单状态转换为前端状态枚举
 */
export function adaptOrderStatus(backendStatus: number): OrderStatus {
  return backendStatus as OrderStatus;
}

/**
 * 将前端状态转换为后端状态码
 */
export function convertOrderStatusToBackend(frontendStatus: OrderStatus): number {
  return frontendStatus;
}

/**
 * 将后端订单数据转换为前端Order格式
 * 处理两种结构：扁平Order 和 OrderDetailResponse{order, orderItems}
 */
export function adaptOrder(backendData: any): Order {
  if (!backendData) return null as any;

  // 处理 OrderDetailResponse 嵌套结构
  const order = backendData.order || backendData;

  // 处理 orderItems - 可能在顶层或嵌套在 orderItems 数组中
  let items = backendData.items || order.items || [];
  if (backendData.orderItems && Array.isArray(backendData.orderItems)) {
    items = backendData.orderItems.map((item: any) => ({
      productId: Number(item.productId),
      name: item.productName || item.name,
      price: Number(item.price),
      quantity: Number(item.quantity),
      image: item.productImage || item.image,
    }));
  } else if (Array.isArray(items)) {
    items = items.map((item: any) => ({
      productId: Number(item.productId),
      name: item.name,
      price: Number(item.price),
      quantity: Number(item.quantity),
      image: item.image,
    }));
  }

  // 处理 address
  let address = backendData.address || order.address;
  if (address && typeof address === 'object' && !address.detail) {
    address = {
      contactName: address.contactName,
      contactPhone: address.contactPhone,
      detail: address.detail || order.deliveryAddress,
    };
  }

  return {
    id: Number(order.id),
    orderNo: order.orderNo,
    merchantId: Number(order.merchantId),
    merchantName: order.merchantName || backendData.merchantName || '',
    merchantLogo: order.merchantLogo,
    userId: Number(order.userId),
    status: adaptOrderStatus(order.status),
    payStatus: order.payStatus,
    totalAmount: Number(order.totalAmount) || 0,
    actualAmount: Number(order.actualAmount) || Number(order.totalAmount) || 0,
    remark: order.remark || '',
    items: items,
    address: address ? {
      contactName: address.contactName,
      contactPhone: address.contactPhone,
      detail: address.detail || order.deliveryAddress,
    } : undefined,
    createTime: order.createTime || order.createdAt || new Date().toISOString(),
    updateTime: order.updateTime,
    payTime: order.payTime,
    acceptTime: order.acceptTime,
    finishTime: order.finishTime,
  };
}

/**
 * 批量适配订单列表
 */
export function adaptOrderList(backendList: any[]): Order[] {
  if (!Array.isArray(backendList)) return [];
  return backendList.map(adaptOrder);
}

// ==================== 用户数据适配 ====================

/**
 * 将后端用户数据转换为前端User格式
 */
export function adaptUser(backendData: any): User {
  if (!backendData) return null as any;
  
  return {
    id: Number(backendData.id),
    phone: backendData.phone,
    nickname: backendData.nickname || backendData.username || '用户' + backendData.phone?.slice(-4),
    avatar: backendData.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + backendData.id,
    gender: backendData.gender,
    userType: backendData.userType,
    status: backendData.status,
    lastLoginTime: backendData.lastLoginTime,
    registerTime: backendData.createTime || backendData.registerTime,
  };
}

// ==================== 分页数据适配 ====================

/**
 * 统一处理分页响应
 * 后端可能返回不同的字段名（merchantList/productList/orderList/list）
 */
export function adaptPaginatedResponse<T>(
  backendData: any,
  itemAdapter: (item: any) => T
): { list: T[]; total: number; page: number; pageSize: number; totalPages?: number } {
  if (!backendData) {
    return { list: [], total: 0, page: 1, pageSize: 10 };
  }
  
  const rawList = backendData.merchantList 
    || backendData.productList 
    || backendData.orderList 
    || backendData.list 
    || [];
  
  return {
    list: rawList.map(itemAdapter),
    total: Number(backendData.total) || 0,
    page: Number(backendData.page) || 1,
    pageSize: Number(backendData.pageSize) || 10,
    totalPages: backendData.totalPages,
  };
}
