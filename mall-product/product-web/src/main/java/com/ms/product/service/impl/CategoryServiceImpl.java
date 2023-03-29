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
//        父级ID为0，一级分类，默认为10个
        List<ProductCategory> firstCategories = getBaseMapper().selectList(setCategoryCondition(Collections.singletonList(0L), CategoryLevelEnum.LEVEL_ONE.getLevel(), 10));
//        一级标签不为空
        if (!CollectionUtils.isEmpty(firstCategories)) {
//            取出一级标签的id
            List<Long> firstId = firstCategories.stream().map(ProductCategory::getCategoryId).collect(Collectors.toList());
//            查询二级标签
            List<ProductCategory> secondCategories = getBaseMapper().selectList(setCategoryCondition(firstId, CategoryLevelEnum.LEVEL_TWO.getLevel(), 0));
//            二级标签不为空
            if (!CollectionUtils.isEmpty(secondCategories)) {
//                取出二级标签的id
                List<Long> secondIds = secondCategories.stream().map(ProductCategory::getCategoryId).collect(Collectors.toList());
//                查询三级标签
                List<ProductCategory> thirdCategories = getBaseMapper().selectList(setCategoryCondition(secondIds, CategoryLevelEnum.LEVEL_THREE.getLevel(), 0));
//                三级标签不为空
                if (!CollectionUtils.isEmpty(thirdCategories)) {
//                    根据所属父级标签id,对三级标签分组
                    Map<Long, List<ProductCategory>> thirdMap = thirdCategories.stream().collect(Collectors.groupingBy(ProductCategory::getParentId));
//                    创建二级标签的视图列表
                    List<SecondLevelCategoryVO> secondLevelCategoryVOS = new ArrayList<>();
                    // 处理二级分类
                    for (ProductCategory productCategory: secondCategories) {
//                        创建二级标签的视图对象
                        SecondLevelCategoryVO secondLevelCategoryVO = new SecondLevelCategoryVO();
//                        复制属性值
                        BeanUtil.copyProperties(productCategory, secondLevelCategoryVO);
//                        查询该二级标签下的三级标签
                        if (thirdMap.containsKey(productCategory.getCategoryId())) {
                            List<ProductCategory> productCategories = thirdMap.get(productCategory.getCategoryId());
//                            设置到二级标签的属性中
                            secondLevelCategoryVO.setThirdLevelCategoryVOS(BeanUtil.copyList(productCategories, ThirdLevelCategoryVO.class));
                        }
                        secondLevelCategoryVOS.add(secondLevelCategoryVO);
                    }
                    // 处理一级分类
                    if (!CollectionUtils.isEmpty(secondLevelCategoryVOS)) {
//                        根据所属父级标签id,对二级标签分组
                        Map<Long, List<SecondLevelCategoryVO>> collect = secondLevelCategoryVOS.stream().collect(Collectors.groupingBy(SecondLevelCategoryVO::getParentId));
//                        遍历一级标签
                        for (ProductCategory productCategory: firstCategories) {
                            IndexCategoryVO indexCategoryVO = new IndexCategoryVO();
                            BeanUtil.copyProperties(productCategory, indexCategoryVO);
//                            查询每个一级标签下的二级标签
                            if (collect.containsKey(productCategory.getCategoryId())) {
                                List<SecondLevelCategoryVO> secondLevelCategoryVOS1 = collect.get(productCategory.getCategoryId());
                                indexCategoryVO.setSecondLevelCategoryVOS(secondLevelCategoryVOS1);
                            }
                            indexCategoryVOS.add(indexCategoryVO);
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
        queryWrapper.in("parent_id", parentId)
                .eq("category_level", level)
                .orderByDesc("category_rank")
                .last(limit > 0,"limit " + limit);
        return queryWrapper;
    }
}
