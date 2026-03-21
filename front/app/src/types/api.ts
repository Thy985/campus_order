/**
 * API 请求/响应类型定义
 * 与后端接口对齐
 */

// ==================== 基础类型 ====================

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface PaginatedResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ==================== 认证模块 ====================

export interface RegisterRequest {
  email: string;
  password: string;
  verifyCode: string;
  nickname?: string;
}

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
  expireTime?: number;
}

export interface VerifyCodeRequest {
  email: string;
}

// ==================== 用户模块 ====================

export interface User {
  id: number;
  username?: string;
  nickname?: string;
  phone: string;
  email?: string;
  avatar?: string;
  gender?: number;        // 0-未知, 1-男, 2-女
  userType: UserType;
  status: number;         // 0-禁用, 1-正常
  lastLoginTime?: string;
  registerTime?: string;
}

export enum UserType {
  USER = 0,
  MERCHANT = 1,
  ADMIN = 2,
}

export interface UpdateUserRequest {
  nickname?: string;
  avatar?: string;
  gender?: number;
}

export interface UpdatePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// ==================== 商家模块 ====================

export interface Merchant {
  id: number;
  name: string;
  logo?: string;
  banner?: string;
  categoryId: number;
  description?: string;
  notice?: string;
  phone?: string;
  avgPrice?: number;
  rating: number;
  salesVolume: number;
  status: MerchantStatus;
}

export enum MerchantStatus {
  CLOSED = 0,
  OPEN = 1,
}

export interface MerchantListRequest {
  page?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
  status?: MerchantStatus;
}

export interface MerchantDetailResponse extends Merchant {
  businessHours?: BusinessHours;
  categories?: Category[];
}

export interface BusinessHours {
  openTime: string;
  closeTime: string;
  isOpen: boolean;
}

// ==================== 商品模块 ====================

export interface Product {
  id: number;
  merchantId: number;
  categoryId: number;
  name: string;
  subtitle?: string;
  image?: string;
  images?: string[];
  description?: string;
  price: number;
  originalPrice?: number;
  unit?: string;
  stock: number;
  salesVolume: number;
  status: ProductStatus;
}

export enum ProductStatus {
  OFFLINE = 0,
  ONLINE = 1,
}

export interface ProductListRequest {
  merchantId: number;
  categoryId?: number;
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: ProductStatus;
  minPrice?: number;
  maxPrice?: number;
}

export interface ProductDetailResponse extends Product {
  merchant?: Merchant;
  category?: Category;
}

// ==================== 订单模块 ====================

export interface Order {
  id: number;
  orderNo: string;
  userId: number;
  merchantId: number;
  merchantName?: string;
  merchantLogo?: string;
  totalAmount: number;
  actualAmount: number;
  remark?: string;
  status: OrderStatus;
  payStatus: PayStatus;
  payTime?: string;
  acceptTime?: string;
  finishTime?: string;
  createTime: string;
  updateTime?: string;
  // 订单详情相关字段
  items?: OrderItem[];
  address?: {
    contactName: string;
    contactPhone: string;
    detail: string;
  };
}

export enum OrderStatus {
  PENDING_PAYMENT = 1,    // 待支付
  PENDING_ACCEPTANCE = 2, // 待接单
  PREPARING = 3,          // 制作中
  READY_FOR_PICKUP = 4,   // 待取餐
  COMPLETED = 5,          // 已完成
  CANCELLED = 6,          // 已取消
  REFUSED = 7,            // 已拒绝
}

export enum PayStatus {
  UNPAID = 0,
  PAID = 1,
}

// 订单状态显示配置
export const OrderStatusConfig: Record<OrderStatus, { label: string; color: string }> = {
  [OrderStatus.PENDING_PAYMENT]: { label: '待支付', color: 'text-amber-600' },
  [OrderStatus.PENDING_ACCEPTANCE]: { label: '待接单', color: 'text-blue-600' },
  [OrderStatus.PREPARING]: { label: '制作中', color: 'text-purple-600' },
  [OrderStatus.READY_FOR_PICKUP]: { label: '待取餐', color: 'text-emerald-600' },
  [OrderStatus.COMPLETED]: { label: '已完成', color: 'text-gray-600' },
  [OrderStatus.CANCELLED]: { label: '已取消', color: 'text-red-500' },
  [OrderStatus.REFUSED]: { label: '已拒绝', color: 'text-red-600' },
};

export interface OrderItem {
  id?: number;
  productId: number;
  name: string;
  image?: string;
  price: number;
  quantity: number;
  totalPrice?: number;
}

export interface CreateOrderRequest {
  merchantId: number;
  deliveryAddress: string;
  contactPhone: string;
  contactName?: string;
  remark?: string;
  orderItems: OrderItemRequest[];
}

export interface OrderItemRequest {
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
}

export interface OrderListRequest {
  status?: OrderStatus;
  page?: number;
  pageSize?: number;
}

export interface OrderDetailResponse extends Order {
  items: OrderItem[];
  merchant: Merchant;
}

export interface CancelOrderRequest {
  reason?: string;
}

// ==================== 地址模块 ====================

export interface Address {
  id: number;
  userId: number;
  contactName: string;
  contactPhone: string;
  province?: string;
  city?: string;
  district?: string;
  detail: string;
  isDefault: number;    // 0-否, 1-是
}

export interface AddressRequest {
  contactName: string;
  contactPhone: string;
  province?: string;
  city?: string;
  district?: string;
  detail: string;
  isDefault?: number;
}

// ==================== 分类模块 ====================

export interface Category {
  id: number;
  name: string;
  icon?: string;
  sortOrder?: number;
}

// ==================== 错误码 ====================

export enum ErrorCode {
  SUCCESS = 200,
  BAD_REQUEST = 400,
  UNAUTHORIZED = 401,
  FORBIDDEN = 403,
  NOT_FOUND = 404,
  INTERNAL_ERROR = 500,

  // 用户相关
  VERIFY_CODE_ERROR = 1001,
  VERIFY_CODE_EXPIRED = 1002,
  PHONE_EXIST = 1003,
  PHONE_NOT_EXIST = 1004,
  PASSWORD_ERROR = 1005,
}

// ==================== 工具类型 ====================

// 将后端 snake_case 转换为前端 camelCase 的工具类型
export type CamelCase<S extends string> = S extends `${infer P}_${infer Q}`
  ? `${P}${Capitalize<CamelCase<Q>>}`
  : S;

// API 错误
export class ApiError extends Error {
  constructor(
    public code: number,
    message: string,
    public data?: unknown
  ) {
    super(message);
    this.name = 'ApiError';
  }
}
