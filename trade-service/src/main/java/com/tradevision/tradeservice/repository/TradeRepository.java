package com.tradevision.tradeservice.repository;

import com.tradevision.tradeservice.model.Trade;
import com.tradevision.tradeservice.model.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByUserIdOrderByExecutedAtDesc(Long userId);

    List<Trade> findBySymbolOrderByExecutedAtDesc(String symbol);

    List<Trade> findByUserIdAndSymbolOrderByExecutedAtDesc(Long userId, String symbol);

    List<Trade> findByUserIdAndStatusOrderByExecutedAtDesc(Long userId, TradeStatus status);

    Optional<Trade> findByIdAndUserId(Long id, Long userId);
}
