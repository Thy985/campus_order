import { get, post, put, del } from '@/lib/request';
import type { Address, AddressRequest } from '@/types';

export type { Address, AddressRequest };

/**
 * 获取用户地址列表
 * 与后端对齐：GET /api/address/list
 */
export async function getUserAddresses(): Promise<Address[]> {
  return get('/api/address/list');
}

/**
 * 获取地址详情
 * 与后端对齐：GET /api/address/{addressId}
 */
export async function getAddressDetail(addressId: number): Promise<Address> {
  return get(`/api/address/${addressId}`);
}

/**
 * 创建地址
 * 与后端对齐：POST /api/address/create
 */
export async function createAddress(data: AddressRequest): Promise<Address> {
  return post('/api/address/create', data);
}

/**
 * 更新地址
 * 与后端对齐：PUT /api/address/update/{addressId}
 */
export async function updateAddress(
  addressId: number,
  data: AddressRequest
): Promise<Address> {
  return put(`/api/address/update/${addressId}`, data);
}

/**
 * 删除地址
 * 与后端对齐：DELETE /api/address/delete/{addressId}
 */
export async function deleteAddress(addressId: number): Promise<void> {
  return del(`/api/address/delete/${addressId}`);
}

/**
 * 设置默认地址
 * 与后端对齐：PUT /api/address/default/{addressId}
 */
export async function setDefaultAddress(addressId: number): Promise<void> {
  return put(`/api/address/default/${addressId}`);
}
