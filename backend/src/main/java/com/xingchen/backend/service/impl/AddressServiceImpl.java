package com.xingchen.backend.service.impl;

import com.xingchen.backend.entity.Address;
import com.xingchen.backend.mapper.AddressMapper;
import com.xingchen.backend.service.AddressService;
import com.xingchen.backend.dto.request.address.AddressRequest;
import com.xingchen.backend.util.constant.Constants;
import com.xingchen.backend.util.constant.ErrorCode;
import com.xingchen.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货地址服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address createAddress(AddressRequest request, Long userId) {
        // 检查地址数量限制（最多10个）
        Long count = addressMapper.countByUserId(userId);
        if (count >= 10) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "收货地址最多10个");
        }

        // 如果设置为默认地址，取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressMapper.cancelDefault(userId);
        }

        // 创建地址
        Address address = new Address();
        address.setUserId(userId);
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault() ? 1 : 0);
        address.setIsDeleted(Constants.DeleteFlag.NOT_DELETED);
        address.setCreateTime(LocalDateTime.now());
        address.setUpdateTime(LocalDateTime.now());

        addressMapper.insert(address);

        log.info("创建收货地址成功: addressId={}, userId={}", address.getId(), userId);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address updateAddress(Long addressId, AddressRequest request, Long userId) {
        // 查询地址
        Address address = addressMapper.selectOneById(addressId);
        if (address == null || address.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "地址不存在");
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此地址");
        }

        // 如果设置为默认地址，取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault()) && address.getIsDefault() == 0) {
            addressMapper.cancelDefault(userId);
        }

        // 更新地址
        address.setContactName(request.getContactName());
        address.setContactPhone(request.getContactPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault() ? 1 : 0);
        address.setUpdateTime(LocalDateTime.now());

        addressMapper.update(address);

        log.info("更新收货地址成功: addressId={}, userId={}", addressId, userId);
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId, Long userId) {
        // 查询地址
        Address address = addressMapper.selectOneById(addressId);
        if (address == null || address.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "地址不存在");
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此地址");
        }

        // 逻辑删除
        address.setIsDeleted(Constants.DeleteFlag.DELETED);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.update(address);

        log.info("删除收货地址成功: addressId={}, userId={}", addressId, userId);
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getAddressDetail(Long addressId, Long userId) {
        Address address = addressMapper.selectOneById(addressId);
        if (address == null || address.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权查看此地址");
        }

        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId, Long userId) {
        // 查询地址
        Address address = addressMapper.selectOneById(addressId);
        if (address == null || address.getIsDeleted() == Constants.DeleteFlag.DELETED) {
            throw new BusinessException(ErrorCode.DATA_NOT_EXIST, "地址不存在");
        }

        // 验证地址归属
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_NO_PERMISSION, "无权操作此地址");
        }

        // 取消其他默认地址
        addressMapper.cancelDefault(userId);

        // 设置当前为默认
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        addressMapper.update(address);

        log.info("设置默认地址成功: addressId={}, userId={}", addressId, userId);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectDefaultByUserId(userId);
    }

    @Override
    public boolean validateAddress(Long addressId, Long userId) {
        Address address = addressMapper.selectOneById(addressId);
        return address != null
                && address.getIsDeleted() == Constants.DeleteFlag.NOT_DELETED
                && address.getUserId().equals(userId);
    }
}
