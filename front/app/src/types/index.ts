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
export interface CartItem {
  dish: Dish;
  quantity: number;
  selected: boolean;
}

export interface Cart {
  storeId: number;   // 修改为number
  items: CartItem[];
  totalCount: number;
  totalPrice: number;
}

// ==================== 订单相关类型 ====================
export interface OrderItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  image?: string;
  dishImage?: string;
  dishName?: string;
}

export interface OrderAddress {
  contactName: string;
  contactPhone: string;
  detail: string;
}

// 注意：Order接口从api.ts导入，避免重复定义
export interface Order {
  id: number;              // 修改为number与后端一致
  orderNo: string;
  merchantId: number;
  merchantName: string;
  merchantLogo?: string;
  userId: number;          // 修改为number
  status: number;          // 修改为number与后端一致
  payStatus?: number;      // 修改为number
  totalAmount: number;
  actualAmount: number;
  remark?: string;
  items: OrderItem[];
  address?: OrderAddress;
  createTime: string;
  updateTime?: string;
  payTime?: string;
  acceptTime?: string;
  finishTime?: string;
}

// 创建订单参数
export interface CreateOrderParams {
  merchantId: number;
  items: {
    productId: number;
    quantity: number;
  }[];
  remark?: string;
  addressId?: number;
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
