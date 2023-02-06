package com.ms.product;

import com.ms.common.api.CommonResult;
import com.ms.product.dto.ProductDTO;
import com.ms.product.dto.UpdateStockNumDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "mall-product-service", path = "/goods")
public interface ProductServiceFeign {

    @GetMapping(value = "/admin/goodsDetail")
    CommonResult<ProductDTO> getGoodsDetail(@RequestParam(value = "goodsId") Long goodsId);

    @GetMapping(value = "/admin/listByGoodsIds")
    CommonResult<List<ProductDTO>> listByGoodsIds(@RequestParam(value = "goodsIds") List<Long> goodsIds);

    @PutMapping(value = "/admin/updateStock")
    CommonResult<Boolean> updateStock(@RequestBody UpdateStockNumDTO updateStockNumDTO);
}
