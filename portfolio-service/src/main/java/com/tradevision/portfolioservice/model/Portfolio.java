package com.tradevision.portfolioservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_positions", indexes = {
        @Index(name = "idx_portfolio_user", columnList = "userId"),
        @Index(name = "idx_portfolio_user_symbol", columnList = "userId,symbol", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String symbol;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal avgBuyPrice;

    @Column(precision = 15, scale = 4)
    private BigDecimal currentPrice;

    @Column(precision = 20, scale = 4)
    private BigDecimal currentValue;

    @Column(precision = 20, scale = 4)
    private BigDecimal profitLoss;

    @Column(precision = 8, scale = 4)
    private BigDecimal profitLossPercent;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
