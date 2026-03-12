import { get, post, put, del } from '@/lib/request';
import type {
  User,
  UpdateUserRequest,
  UpdatePasswordRequest,
  Address,
  AddressRequest,
} from '@/types';

/**
 * 用户相关 API
 */

/**
 * 获取用户信息
 * 修改为与后端对齐：GET /api/user/profile
 */
export async function getUserProfile(): Promise<User> {
  return get<User>('/api/user/profile');
}

/**
 * 更新用户信息
 * 修改为与后端对齐：PUT /api/user/profile
 */
export async function updateUserProfile(data: UpdateUserRequest): Promise<User> {
  return put<User>('/api/user/profile', data);
}

/**
 * 修改密码
 * 修改为与后端对齐：PUT /api/user/password
 */
export async function updatePassword(data: UpdatePasswordRequest): Promise<void> {
  return put<void>('/api/user/password', data);
}

// ==================== 地址管理 ====================

/**
 * 获取用户地址列表
 * 修改为与后端对齐：GET /api/address/list
 */
export async function getUserAddresses(): Promise<Address[]> {
  const result = await get<Address[]>('/api/address/list');
  return result || [];
}

/**
 * 获取地址详情
 * 注意：后端需要实现此接口 GET /api/address/{addressId}
 */
export async function getAddressDetail(addressId: number): Promise<Address> {
  return get<Address>(`/api/address/${addressId}`);
}

/**
 * 创建地址
 * 修改为与后端对齐：POST /api/address/create
 */
export async function createAddress(data: AddressRequest): Promise<Address> {
  return post<Address>('/api/address/create', data);
}

/**
 * 更新地址
 * 修改为与后端对齐：PUT /api/address/update/{addressId}
 */
export async function updateAddress(
  addressId: number,
  data: AddressRequest
): Promise<Address> {
  return put<Address>(`/api/address/update/${addressId}`, data);
}

/**
 * 删除地址
 * 修改为与后端对齐：DELETE /api/address/delete/{addressId}
 */
export async function deleteAddress(addressId: number): Promise<void> {
  return del<void>(`/api/address/delete/${addressId}`);
}
