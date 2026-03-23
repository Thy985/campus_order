package com.xingchen.backend.service;

import com.xingchen.backend.entity.Address;
import com.xingchen.backend.dto.request.address.AddressRequest;

import java.util.List;

/**
 * 收货地址服务接口
 */
public interface AddressService {

    /**
     * 创建收货地址
     */
    Address createAddress(AddressRequest request, Long userId);

    /**
     * 更新收货地址
     */
    Address updateAddress(Long addressId, AddressRequest request, Long userId);

    /**
     * 删除收货地址
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * 获取用户的收货地址列表
     */
    List<Address> getUserAddresses(Long userId);

    /**
     * 获取地址详情
     */
    Address getAddressDetail(Long addressId, Long userId);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(Long addressId, Long userId);

    /**
     * 获取用户的默认地址
     */
    Address getDefaultAddress(Long userId);

    /**
     * 验证地址是否存在且属于用?     */
    boolean validateAddress(Long addressId, Long userId);
}
