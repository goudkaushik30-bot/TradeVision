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
public class AlertDto {

    private Long id;
    private Long userId;
    private String symbol;
    private String alertType;
    private BigDecimal targetPrice;
    private boolean active;
    private LocalDateTime triggeredAt;
    private LocalDateTime createdAt;
}
