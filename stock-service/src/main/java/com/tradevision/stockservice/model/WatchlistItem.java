package com.tradevision.stockservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items", indexes = {
        @Index(name = "idx_watchlist_user", columnList = "userId"),
        @Index(name = "idx_watchlist_user_symbol", columnList = "userId,stockSymbol", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String stockSymbol;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
