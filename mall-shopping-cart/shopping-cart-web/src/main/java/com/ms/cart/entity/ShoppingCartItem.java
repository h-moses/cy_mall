package com.ms.cart.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class ShoppingCartItem {
    private Long cartItemId;

    private Long userId;

    private Long goodsId;

    private Integer goodsCount;

    @TableLogic
    private Byte isDeleted;

    private Date createTime;

    private Date updateTime;
}
