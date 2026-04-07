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
public class StockPriceEvent {

    private String symbol;
    private BigDecimal previousPrice;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private Long volume;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
