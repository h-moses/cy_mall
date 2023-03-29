package com.ms.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.product.entity.Product;
import com.ms.product.entity.StockNumDTO;

import java.util.List;

public interface ProductService extends IService<Product> {

    String saveProduct(Product product);

    String updateProduct(Product product);

    boolean updateStatus(Long[] ids, int status);

    int updateStock(List<StockNumDTO> stocks, boolean isCancel);
}
