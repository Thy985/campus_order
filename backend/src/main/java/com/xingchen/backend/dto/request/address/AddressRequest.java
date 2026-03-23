package com.xingchen.backend.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 收货地址请求DTO
 */
@Data
public class AddressRequest {

    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 50, message = "联系人姓名最多50个字符")
    private String contactName;

    @NotBlank(message = "联系人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String contactPhone;

    private String province;

    private String city;

    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址最多200个字符")
    private String detailAddress;

    private Boolean isDefault;
}
