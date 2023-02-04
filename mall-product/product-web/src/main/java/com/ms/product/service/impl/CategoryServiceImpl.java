package com.ms.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.common.enums.CategoryLevelEnum;
import com.ms.common.utils.BeanUtil;
import com.ms.product.controller.vo.IndexCategoryVO;
import com.ms.product.controller.vo.SecondLevelCategoryVO;
import com.ms.product.controller.vo.ThirdLevelCategoryVO;
import com.ms.product.entity.ProductCategory;
import com.ms.product.mapper.CategoryMapper;
import com.ms.product.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, ProductCategory> implements CategoryService {

    @Override
    public List<IndexCategoryVO> getCategoriesForIndex() {
        List<IndexCategoryVO>indexCategoryVOS = new ArrayList<>();
        List<ProductCategory> firstCategories = getBaseMapper().selectList(setCategoryCondition(Collections.singletonList(0L), CategoryLevelEnum.LEVEL_ONE.getLevel(), 10));
        if (!CollectionUtils.isEmpty(firstCategories)) {
            List<Long> firstId = firstCategories.stream().map(ProductCategory::getCategoryId).collect(Collectors.toList());
            List<ProductCategory> secondCategories = getBaseMapper().selectList(setCategoryCondition(firstId, CategoryLevelEnum.LEVEL_TWO.getLevel(), 0));
            if (!CollectionUtils.isEmpty(secondCategories)) {
                List<Long> secondIds = secondCategories.stream().map(ProductCategory::getCategoryId).collect(Collectors.toList());
                List<ProductCategory> thirdCategories = getBaseMapper().selectList(setCategoryCondition(secondIds, CategoryLevelEnum.LEVEL_THREE.getLevel(), 0));
                if (!CollectionUtils.isEmpty(thirdCategories)) {
                    Map<Long, List<ProductCategory>> thirdMap = thirdCategories.stream().collect(Collectors.groupingBy(ProductCategory::getParentId));
                    List<SecondLevelCategoryVO> secondLevelCategoryVOS = new ArrayList<>();
                    // 处理二级分类
                    for (ProductCategory productCategory: secondCategories) {
                        SecondLevelCategoryVO secondLevelCategoryVO = new SecondLevelCategoryVO();
                        BeanUtil.copyProperties(productCategory, secondLevelCategoryVO);
                        if (thirdMap.containsKey(productCategory.getCategoryId())) {
                            List<ProductCategory> productCategories = thirdMap.get(productCategory.getParentId());
                            secondLevelCategoryVO.setThirdLevelCategoryVOS(BeanUtil.copyList(productCategories, ThirdLevelCategoryVO.class, null));
                            secondLevelCategoryVOS.add(secondLevelCategoryVO);
                        }
                    }
                    // 处理一级分类
                    if (!CollectionUtils.isEmpty(secondLevelCategoryVOS)) {
                        Map<Long, List<SecondLevelCategoryVO>> collect = secondLevelCategoryVOS.stream().collect(Collectors.groupingBy(SecondLevelCategoryVO::getParentId));
                        for (ProductCategory productCategory: firstCategories) {
                            IndexCategoryVO indexCategoryVO = new IndexCategoryVO();
                            BeanUtil.copyProperties(productCategory, indexCategoryVO);
                            if (collect.containsKey(productCategory.getCategoryId())) {
                                List<SecondLevelCategoryVO> secondLevelCategoryVOS1 = collect.get(productCategory.getCategoryId());
                                indexCategoryVO.setSecondLevelCategoryVOS(secondLevelCategoryVOS1);
                                indexCategoryVOS.add(indexCategoryVO);
                            }
                        }
                    }
                }
            }
            return indexCategoryVOS;
        } else {
            return null;
        }
    }

    public QueryWrapper<ProductCategory> setCategoryCondition(List<Long> parentId, int level, int limit) {
        QueryWrapper<ProductCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("parent_id", parentId);
        queryWrapper.eq("category_level", level);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("category_rank");
        queryWrapper.last("limit " + limit);
        return queryWrapper;
    }
}
