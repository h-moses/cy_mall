package com.ms.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockDTO {

    private List<StockDTO> stockDTOS;

    public List<StockDTO> getStockDTOS() {
        return stockDTOS;
    }

    public void setStockDTOS(List<StockDTO> stockDTOS) {
        this.stockDTOS = stockDTOS;
    }
}
