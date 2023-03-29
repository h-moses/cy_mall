package com.ms.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.product.entity.Product;
import com.ms.product.entity.StockNumDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Update({"update `mall_goods_info` set goods_sell_status=#{status} where goods_id in #{prodIds}"})
    int batchUpdateStatus(@Param("prodIds") Long[] ids, @Param("status") int status);

    int updateStock(@Param("stocks") List<StockNumDTO> stocks);

    int increaseStock(@Param("stocks") List<StockNumDTO> stocks);
}
