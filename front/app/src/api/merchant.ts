import { get, post, put, del } from '@/lib/request';

// ==================== 商家商品管理接口 ====================

export interface ProductListRequest {
  page?: number;
  pageSize?: number;
  categoryId?: number;
  status?: number;
  merchantId?: number;
  keyword?: string;
}

export interface ProductDetail {
  id: number;
  name: string;
  description: string;
  price: number;
  image: string;
  categoryId: number;
  status: number;
  salesVolume: number;
  createTime: string;
}

export interface ProductCategory {
  id: number;
  name: string;
  sort: number;
}

export interface ProductListResponse {
  total: number;
  page: number;
  pageSize: number;
  productList: ProductDetail[];
}

export interface CreateProductRequest {
  name: string;
  description?: string;
  image?: string;
  price: number;
  originalPrice?: number;
  stock: number;
  category?: string;
  status?: number;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  image?: string;
  price?: number;
  originalPrice?: number;
  stock?: number;
  category?: string;
  status?: number;
}

/**
 * 获取商品列表
 * GET /api/merchant/product/list
 */
export async function getProductList(params: ProductListRequest): Promise<ProductListResponse> {
  return get<ProductListResponse>('/api/merchant/product/list', { params });
}

/**
 * 获取商品详情
 * GET /api/merchant/product/detail/{id}
 */
export async function getProductDetail(id: number): Promise<ProductDetail> {
  return get<ProductDetail>(`/api/merchant/product/detail/${id}`);
}

/**
 * 获取商品分类
 * GET /api/merchant/products/categories
 */
export async function getProductCategories(merchantId?: number): Promise<ProductCategory[]> {
  return get<ProductCategory[]>('/api/merchant/products/categories', { params: { merchantId } });
}

/**
 * 创建商品
 * POST /api/merchant/product/create
 */
export async function createProduct(data: CreateProductRequest): Promise<ProductDetail> {
  return post<ProductDetail>('/api/merchant/product/create', data);
}

/**
 * 更新商品
 * PUT /api/merchant/product/update/{id}
 */
export async function updateProduct(id: number, data: UpdateProductRequest): Promise<ProductDetail> {
  return put<ProductDetail>(`/api/merchant/product/update/${id}`, data);
}

/**
 * 删除商品
 * DELETE /api/merchant/product/delete/{id}
 */
export async function deleteProduct(id: number): Promise<void> {
  return del<void>(`/api/merchant/product/delete/${id}`);
}

/**
 * 上架商品
 * POST /api/merchant/product/on-shelf/{id}
 */
export async function publishProduct(id: number): Promise<void> {
  return post<void>(`/api/merchant/product/on-shelf/${id}`);
}

/**
 * 下架商品
 * POST /api/merchant/product/off-shelf/{id}
 */
export async function unpublishProduct(id: number): Promise<void> {
  return post<void>(`/api/merchant/product/off-shelf/${id}`);
}

/**
 * 获取商家商品列表（兼容旧接口名）
 */
export async function getMerchantProducts(params: ProductListRequest): Promise<ProductListResponse> {
  return getProductList(params);
}

export interface MerchantListRequest {
  page?: number;
  pageSize?: number;
  categoryId?: number;
  keyword?: string;
  status?: number;
}

export interface MerchantDetailResponse {
  id: number;
  name: string;
  logo: string;
  rating: number;
  salesVolume: number;
  deliveryTime: number;
  distance: number;
  minPrice: number;
  minOrderAmount?: number;
  status: number;
  categoryId?: number;
  description?: string;
  address?: string;
  phone?: string;
  banner?: string;
}

export interface MerchantListResponse {
  total: number;
  page: number;
  pageSize: number;
  merchantList: MerchantDetailResponse[];
}

/**
 * 获取商家列表
 * GET /api/merchant/list
 */
export async function getMerchantList(params: MerchantListRequest): Promise<MerchantListResponse> {
  return get<MerchantListResponse>('/api/merchant/list', { params });
}

/**
 * 获取商家详情
 * GET /api/merchant/detail/{id}
 */
export async function getMerchantDetail(id: number): Promise<MerchantDetailResponse> {
  return get<MerchantDetailResponse>(`/api/merchant/detail/${id}`);
}

// ==================== 商家信息设置接口 ====================

export interface MerchantSettings {
  id: number;
  name: string;
  logo: string;
  banner?: string;
  phone: string;
  address: string;
  description?: string;
  notice?: string;
  minPrice: number;
  packagingFee: number;
  status: number;
  businessHours?: BusinessHoursSlot[];
}

export interface BusinessHoursSlot {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  isOpen: boolean;
}

export interface UpdateMerchantSettingsRequest {
  name?: string;
  logo?: string;
  banner?: string;
  phone?: string;
  address?: string;
  description?: string;
  notice?: string;
  minPrice?: number;
  packagingFee?: number;
  status?: number;
  businessHours?: BusinessHoursSlot[];
}

/**
 * 获取当前商家信息
 * GET /api/merchant/info
 */
export async function getCurrentMerchantInfo(): Promise<MerchantSettings> {
  return get<MerchantSettings>('/api/merchant/info');
}

/**
 * 更新商家信息
 * PUT /api/merchant/info
 */
export async function updateMerchantInfo(data: UpdateMerchantSettingsRequest): Promise<MerchantSettings> {
  return put<MerchantSettings>('/api/merchant/info', data);
}

/**
 * 获取商家营业时间
 * GET /api/merchant/business-hours
 */
export async function getBusinessHours(): Promise<BusinessHoursSlot[]> {
  return get<BusinessHoursSlot[]>('/api/merchant/business-hours');
}

/**
 * 设置商家营业时间
 * POST /api/merchant/business-hours
 */
export async function setBusinessHours(hours: BusinessHoursSlot[]): Promise<void> {
  return post<void>('/api/merchant/business-hours', hours);
}

// ==================== 商家统计接口 ====================

export interface MerchantStatistics {
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
 * GET /api/merchant/statistics
 */
export async function getMerchantStatistics(): Promise<MerchantStatistics> {
  return get<MerchantStatistics>('/api/merchant/statistics');
}

/**
 * 获取营收趋势
 * GET /api/merchant/statistics/revenue
 */
export async function getRevenueTrend(range: 'week' | 'month' | 'year' = 'week'): Promise<{ date: string; amount: number }[]> {
  return get<{ date: string; amount: number }[]>('/api/merchant/statistics/revenue', { params: { range } });
}

/**
 * 获取热销菜品
 * GET /api/merchant/statistics/top-dishes
 */
export async function getTopDishes(limit: number = 5): Promise<{ name: string; salesAmount: number }[]> {
  return get<{ name: string; salesAmount: number }[]>('/api/merchant/statistics/top-dishes', { params: { limit } });
}

// ==================== 商家订单接口 ====================

export interface MerchantOrder {
  id: number;
  orderNo: string;
  userId: number;
  status: number;
  totalAmount: number;
  items: { productId: number; name: string; price: number; quantity: number }[];
  address?: { contactName: string; contactPhone: string; detail: string };
  remark?: string;
  createTime: string;
}

export interface MerchantOrderListResponse {
  total: number;
  page: number;
  pageSize: number;
  orderList: MerchantOrder[];
}

/**
 * 获取商家订单列表
 * GET /api/merchant/orders
 */
export async function getMerchantOrders(params: {
  page?: number;
  pageSize?: number;
  status?: number;
}): Promise<MerchantOrderListResponse> {
  return get<MerchantOrderListResponse>('/api/merchant/orders', { params });
}

/**
 * 接单
 * POST /api/merchant/orders/{orderId}/accept
 */
export async function acceptOrder(orderId: number): Promise<void> {
  return post<void>(`/api/merchant/orders/${orderId}/accept`);
}

/**
 * 拒单
 * POST /api/merchant/orders/{orderId}/reject
 */
export async function rejectOrder(orderId: number, reason?: string): Promise<void> {
  return post<void>(`/api/merchant/orders/${orderId}/reject`, null, { params: { reason } });
}

/**
 * 完成订单
 * POST /api/merchant/orders/{orderId}/complete
 */
export async function completeOrder(orderId: number): Promise<void> {
  return post<void>(`/api/merchant/orders/${orderId}/complete`);
}

/**
 * 获取待处理订单数量
 * GET /api/merchant/orders/pending-count
 */
export async function getPendingOrderCount(): Promise<{
  pendingAcceptance: number;
  preparing: number;
  total: number;
}> {
  return get<{
    pendingAcceptance: number;
    preparing: number;
    total: number;
  }>('/api/merchant/orders/pending-count');
}

