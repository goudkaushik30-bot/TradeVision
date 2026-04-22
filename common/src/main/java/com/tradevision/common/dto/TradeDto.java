package com.tradevision.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {

    private Long id;
    private Long userId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private String tradeType;
    private String status;
    private BigDecimal totalValue;
    private LocalDateTime executedAt;
}
