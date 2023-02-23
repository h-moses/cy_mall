package com.ms.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.utils.BeanUtil;
import com.ms.product.controller.vo.ProductDetailVO;
import com.ms.product.controller.vo.SearchProductVO;
import com.ms.product.entity.Product;
import com.ms.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "商品相关借口")
@RequestMapping("/goods/mall")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Resource
    private ProductService productService;

    @GetMapping("/search")
    @ApiOperation(value = "商品搜索接口", notes = "根据关键字和分类id进行搜索")
    public CommonResult<Page<SearchProductVO>> search(
            @RequestParam(required = false) @ApiParam("搜索关键字") String keyword,
            @RequestParam(required = false) @ApiParam("分类ID") Long prodCateId,
            @RequestParam(required = false) @ApiParam("排序") String orderBy,
            @RequestParam(required = false) @ApiParam("页码") Integer pageNum
    ) {
        logger.info("goods search api,keyword={},goodsCategoryId={},orderBy={},pageNumber={},userId={}", keyword, prodCateId, orderBy, pageNum);

        if (null == prodCateId && !StringUtils.hasText(keyword)) {
            return CommonResult.failure("搜索参数异常");
        }
        if (null == pageNum || pageNum < 1) {
            pageNum = 1;
        }
        Page<Product> page = new Page<>(pageNum, 10);
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(prodCateId.toString()),"goods_category_id", prodCateId);
        queryWrapper.eq("goods_sell_status", 0);
        queryWrapper.and(StringUtils.hasText(keyword), wq -> {
           wq.like("goods_name", keyword)
                   .or()
                   .like("goods_intro", keyword);
        });
        queryWrapper.and(StringUtils.hasText(orderBy), productQueryWrapper -> {
            productQueryWrapper.orderByDesc(orderBy.equals("new"), "goods_id")
                    .or()
                    .orderByAsc(orderBy.equals("price"), "selling_price")
                    .or().
                    orderByDesc("stock_num");
        });
        Page<Product> productPage = productService.page(page, queryWrapper);
        Page<SearchProductVO> searchProductVOPage = new Page<>();
        if (productPage.getTotal() != 0) {
            List<SearchProductVO> searchProductVOS = BeanUtil.copyList(productPage.getRecords(), SearchProductVO.class);
            for (SearchProductVO searchProductVO: searchProductVOS) {
                String goodsName = searchProductVO.getGoodsName();
                String goodsIntro = searchProductVO.getGoodsIntro();
                if (goodsName.length() > 28) {
                    goodsName = goodsName.substring(0, 28) + "...";
                    searchProductVO.setGoodsName(goodsName);
                }
                if (goodsIntro.length() > 28) {
                    goodsIntro = goodsIntro.substring(0, 28) + "...";
                    searchProductVO.setGoodsIntro(goodsIntro);
                }
            }
            searchProductVOPage.setRecords(searchProductVOS);
        }
        BeanUtil.copyProperties(productPage, searchProductVOPage, "records");
        return CommonResult.success(searchProductVOPage);
    }


    @GetMapping("/detail/{goodsId}")
    @ApiOperation(value = "商品详情接口", notes = "传参为商品id")
    public CommonResult<ProductDetailVO> getProductDetail(@ApiParam("商品ID") @PathVariable("goodsId") Long goodsId) {
        if (goodsId < 1) {
            return CommonResult.failure("商品ID异常");
        }
        Product product = productService.getById(goodsId);
        if (null == product || 0 != product.getGoodsSellStatus()) {
            return CommonResult.failure(ServiceResultEnum.GOODS_PUT_DOWN.getResult());
        }
        ProductDetailVO productDetailVO = new ProductDetailVO();
        BeanUtil.copyProperties(product, productDetailVO);
        productDetailVO.setGoodsCarouselList(product.getGoodsCarousel().split(","));
        return CommonResult.success(productDetailVO);
    }
}
