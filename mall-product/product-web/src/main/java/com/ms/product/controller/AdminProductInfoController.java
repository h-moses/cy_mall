package com.ms.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ms.common.annotation.TokenToAdminUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.utils.BeanUtil;
import com.ms.product.controller.param.BatchIdParam;
import com.ms.product.controller.param.ProductAddParam;
import com.ms.product.controller.param.ProductEditParam;
import com.ms.product.entity.LoginAdminUser;
import com.ms.product.entity.Product;
import com.ms.product.entity.UpdateStockNumDTO;
import com.ms.product.service.CategoryService;
import com.ms.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "后台管理系统商品模块接口")
@RequestMapping("/goods/admin")
public class AdminProductInfoController {

    @Resource
    private ProductService productService;

    @Resource
    private CategoryService categoryService;


    @GetMapping("/list")
    @ApiOperation(value = "商品列表", notes = "可根据名称和上架状态筛选")
    public CommonResult list(
            @RequestParam(required = false) @ApiParam(value = "页码") Integer pageNum,
            @RequestParam(required = false) @ApiParam(value = "每页条数") Integer pageSize,
            @RequestParam(required = false) @ApiParam(value = "商品名称") String productName,
            @RequestParam(required = false) @ApiParam(value = "上架状态 0-上架 1-下架") Integer status
    ) {
        if (null == pageNum || pageNum < 1 || null == pageSize || pageSize < 10) {
            pageNum = 1;
            pageSize = 10;
        }

        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.like(StringUtils.hasText(productName),"goods_name", productName)
                .eq(StringUtils.hasText(String.valueOf(status)),"goods_sell_status", status)
                .orderByDesc("goods_id");
        Page<Product> productPage = productService.page(new Page<>(pageNum, pageSize), productQueryWrapper);
        return CommonResult.success(productPage);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "新增商品信息", notes = "新增商品信息")
    public CommonResult save(@RequestBody @Valid ProductAddParam productAddParam) {
        Product product = new Product();
        BeanUtil.copyProperties(productAddParam, product);
        String s = productService.saveProduct(product);
        if (s.equals(ServiceResultEnum.SUCCESS.getResult())) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failure(s);
        }
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "修改商品信息", notes = "修改商品信息")
    public CommonResult update(@RequestBody @Valid ProductEditParam productEditParam) {
        Product product = new Product();
        BeanUtil.copyProperties(productEditParam, product);
        String s = productService.updateProduct(product);
        if (s.equals(ServiceResultEnum.SUCCESS.getResult())) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failure(s);
        }
    }

    @PutMapping(value = "/updateStatus/{sellStatus}")
    @ApiOperation(value = "批量修改销售状态", notes = "批量修改销售状态")
    public CommonResult updateStatus(@RequestBody BatchIdParam batchIdParam, @PathVariable("sellStatus") int status) {
        if (null == batchIdParam || batchIdParam.getIds().length < 1) {
            return CommonResult.failure("参数异常");
        }
        if (status != 0 && status != 1) {
            return CommonResult.failure("状态异常");
        }
        if (productService.updateStatus(batchIdParam.getIds(), status)) {
            return CommonResult.success();
        } else {
            return CommonResult.failure("修改失败");
        }
    }

    @GetMapping("/goodsDetail")
    @ApiOperation(value = "获取单条商品信息", notes = "根据id查询")
    public CommonResult goodsDetail(@RequestParam("goodsId") Long id) {
        Product pro = productService.getById(id);
        return CommonResult.success(pro);
    }

    @GetMapping("/listByGoodsIds")
    @ApiOperation(value = "根据ids查询商品列表", notes = "根据ids查询")
    public CommonResult getProByIds(@RequestParam("goodsIds") List<Long> ids) {
        List<Product> products = productService.listByIds(ids);
        return CommonResult.success(products);
    }

    @PutMapping("/updateStock")
    @ApiOperation(value = "修改库存")
    public CommonResult updateStock(@RequestBody UpdateStockNumDTO updateStockNumDTO, @TokenToAdminUser LoginAdminUser adminUser) {
        int i = productService.updateStock(updateStockNumDTO.getStockNumDTOS());
        if (i > 0) {
            return CommonResult.success(i);
        } else {
            return CommonResult.failure("修改失败");
        }
    }
}
