package com.ms.user.api;

import com.ms.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "mall-user-service", path = "/users")
public interface UserServiceFeign {

    @GetMapping(value = "/admin/{token}")
    CommonResult getAdminUserByToken(@PathVariable(value = "token") String token);

    @GetMapping(value = "/mall/getDetailByToken")
    CommonResult getMallUserByToken(@RequestParam(value = "token") String token);
}
