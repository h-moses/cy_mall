package com.ms.product.controller;

import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.product.controller.vo.IndexCategoryVO;
import com.ms.product.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "新蜂商城分类页面接口")
@RequestMapping("/categories/mall")
public class ProductCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductCategoryController.class);

    @Resource
    private CategoryService categoryService;


    @GetMapping("/listAll")
    @ApiOperation(value = "获取分类数据", notes = "分类页面使用")
    public CommonResult<List<IndexCategoryVO>> getCategories() {
        List<IndexCategoryVO> list = categoryService.getCategoriesForIndex();
        if (CollectionUtils.isEmpty(list)) {
            return CommonResult.failure(ServiceResultEnum.DATA_NOT_EXIST.getResult());
        }
        return CommonResult.success(list);
    }
}
