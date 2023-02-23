package com.ms.product.entity;

import lombok.Data;

import java.util.List;

@Data
public class UpdateStockDTO {

    private List<StockDTO> stockDTOS;

    public List<StockDTO> getStockDTOS() {
        return stockDTOS;
    }

    public void setStockDTOS(List<StockDTO> stockDTOS) {
        this.stockDTOS = stockDTOS;
    }
}
