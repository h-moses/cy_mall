package com.ms.product.dto;

import lombok.Data;

import java.util.List;


@Data
public class UpdateStockNumDTO {

    private List<StockNumDTO> stockNumDTOS;
}
