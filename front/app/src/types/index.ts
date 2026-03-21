// Re-export all types from api.ts
export * from './api';

// ==================== 订单状态文本映射 ====================
// 使用 api.ts 中的 OrderStatus 枚举
import { OrderStatus } from './api';

export const OrderStatusText: Record<OrderStatus, string> = {
  [OrderStatus.PENDING_PAYMENT]: '待支付',
  [OrderStatus.PENDING_ACCEPTANCE]: '待接单',
  [OrderStatus.PREPARING]: '制作中',
  [OrderStatus.READY_FOR_PICKUP]: '待取餐',
  [OrderStatus.COMPLETED]: '已完成',
  [OrderStatus.CANCELLED]: '已取消',
  [OrderStatus.REFUSED]: '已拒绝',
};

// 从api.ts重新导出UserType
export { UserType } from './api';

// ==================== 用户相关类型 ====================
export interface UserProfile {
  id: number;        // 修改为number与后端一致
  phone: string;
  nickname?: string;
  avatar?: string;
  role: 'user' | 'merchant' | 'admin';
  createdAt?: string;
  updatedAt?: string;
}

// ==================== 商家相关类型 ====================
export interface Store {
  id: number;        // 修改为number
  name: string;
  logo: string;
  banner?: string;
  rating: number;
  monthlySales: number;
  deliveryTime: number;
  distance: number;
  minPrice: number;
  tags: string[];
  categories: string[];
  address?: string;
  description?: string;
}

export interface Merchant {
  id: number;
  name: string;
  logo: string;
  rating: number;
  monthlySales: number;
  deliveryTime: number;
  distance: number;
  minPrice: number;
  status: number;
  categoryId?: number;
}

// ==================== 商品相关类型 ====================
export interface Dish {
  id: number;        // 修改为number
  storeId: number;   // 修改为number
  name: string;
  description: string;
  price: number;
  image: string;
  category: string;
  isRecommended?: boolean;
  isSpicy?: boolean;
  sales?: number;
  originalPrice?: number;
  tags?: string[];
  isAvailable?: boolean;
}

// ==================== 购物车类型 ====================
// 与 cartStore.tsx 中的 CartItem 保持一致
export interface CartItem {
  productId: number;
  merchantId: number;
  merchantName: string;
  name: string;
  price: number;
  quantity: number;
  image?: string;
  dish?: Dish;
}

export interface Cart {
  storeId: number;   // 修改为number
  items: CartItem[];
  totalCount: number;
  totalPrice: number;
}

// ==================== 订单相关类型 ====================
// 从api.ts重新导出订单相关类型，避免重复定义
export type { Order, OrderItem, OrderDetailResponse } from './api';
export { OrderStatus, PayStatus } from './api';

export interface OrderAddress {
  contactName: string;
  contactPhone: string;
  detail: string;
}

// 创建订单参数
export interface CreateOrderParams {
  merchantId: number;
  orderItems: {
    productId: number;
    productName: string;
    productPrice: number;
    quantity: number;
    productImage?: string;
  }[];
  remark?: string;
  addressId?: number;
  deliveryAddress?: string;
  contactPhone?: string;
  contactName?: string;
}

// ==================== 分页响应类型 ====================
export interface PaginatedResponse<T> {
  // 适配后端不同的字段名
  list?: T[];
  merchantList?: T[];
  productList?: T[];
  orderList?: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages?: number;
}

// ==================== 消息通知类型 ====================
export interface Notification {
  id: number;
  userId: number;
  type: number;
  title: string;
  content: string;
  extraData?: string;
  isRead: number;
  isDeleted: number;
  createTime: string;
}

export enum NotificationType {
  SYSTEM = 0,
  ORDER = 1,
  ACTIVITY = 2,
}

export const NotificationTypeText: Record<NotificationType, string> = {
  [NotificationType.SYSTEM]: '系统通知',
  [NotificationType.ORDER]: '订单通知',
  [NotificationType.ACTIVITY]: '活动通知',
};
