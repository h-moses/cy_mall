package com.ms.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.product.controller.vo.IndexCategoryVO;
import com.ms.product.entity.ProductCategory;

import java.util.List;

public interface CategoryService extends IService<ProductCategory> {

    public List<IndexCategoryVO> getCategoriesForIndex();
}
