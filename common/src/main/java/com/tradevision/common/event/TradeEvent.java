package com.tradevision.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEvent {

    private String tradeId;
    private String userId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal price;
    private String tradeType;
    private String status;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
