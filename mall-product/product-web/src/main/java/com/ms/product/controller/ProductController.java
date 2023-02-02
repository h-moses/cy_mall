package com.ms.product.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Api(tags = "商品相关借口")
@RequestMapping("/goods/mall")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    
}
