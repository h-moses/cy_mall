package com.ms.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.product.entity.Product;
import com.ms.product.entity.StockDTO;

import java.util.List;

public interface ProductService extends IService<Product> {

    public String saveProduct(Product product);

    public String updateProduct(Product product);

    public int updateStatus(Long[] ids, int status);

    public int updateStock(List<StockDTO> stockDTOS);
}
