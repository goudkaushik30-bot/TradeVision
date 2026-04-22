package com.tradevision.stockservice.repository;

import com.tradevision.stockservice.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUserId(Long userId);

    Optional<WatchlistItem> findByUserIdAndStockSymbol(Long userId, String stockSymbol);

    boolean existsByUserIdAndStockSymbol(Long userId, String stockSymbol);

    void deleteByUserIdAndStockSymbol(Long userId, String stockSymbol);
}
