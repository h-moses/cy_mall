package com.ms.cart.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "mall_shopping_cart_item")
public class ShoppingCartItem {
    @TableId(value = "cart_item_id")
    private Long cartItemId;

    private Long userId;

    private Long goodsId;

    private Integer goodsCount;

    @TableLogic
    private Byte isDeleted;

    private Date createTime;

    private Date updateTime;
}
