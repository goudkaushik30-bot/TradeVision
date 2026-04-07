package com.tradevision.tradeservice.service;

import com.tradevision.common.exception.ResourceNotFoundException;
import com.tradevision.common.exception.TradeVisionException;
import com.tradevision.tradeservice.dto.TradeRequest;
import com.tradevision.tradeservice.dto.TradeResponse;
import com.tradevision.tradeservice.kafka.TradeEventProducer;
import com.tradevision.tradeservice.model.Trade;
import com.tradevision.tradeservice.model.TradeStatus;
import com.tradevision.tradeservice.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final TradeEventProducer tradeEventProducer;

    @Transactional
    public TradeResponse executeTrade(Long userId, TradeRequest request) {
        BigDecimal totalValue = request.getQuantity().multiply(request.getPrice());

        Trade trade = Trade.builder()
                .userId(userId)
                .symbol(request.getSymbol().toUpperCase())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .tradeType(request.getTradeType())
                .status(TradeStatus.PENDING)
                .totalValue(totalValue)
                .build();

        Trade saved = tradeRepository.save(trade);

        try {
            // Simulate trade execution (in production, integrate with broker API)
            saved.setStatus(TradeStatus.EXECUTED);
            saved = tradeRepository.save(saved);

            tradeEventProducer.publishTradeEvent(saved);
            log.info("Executed trade {} for user {}: {} {} {} @ {}",
                    saved.getId(), userId, saved.getTradeType(),
                    saved.getQuantity(), saved.getSymbol(), saved.getPrice());
        } catch (Exception e) {
            saved.setStatus(TradeStatus.FAILED);
            tradeRepository.save(saved);
            log.error("Trade execution failed for trade {}: {}", saved.getId(), e.getMessage());
            throw new TradeVisionException("Trade execution failed: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public TradeResponse cancelTrade(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdAndUserId(tradeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));

        if (trade.getStatus() != TradeStatus.PENDING) {
            throw new TradeVisionException(
                    "Only PENDING trades can be cancelled", HttpStatus.BAD_REQUEST);
        }

        trade.setStatus(TradeStatus.CANCELLED);
        Trade saved = tradeRepository.save(trade);
        log.info("Cancelled trade {} for user {}", tradeId, userId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> getTradeHistory(Long userId) {
        return tradeRepository.findByUserIdOrderByExecutedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TradeResponse getTradeById(Long tradeId, Long userId) {
        Trade trade = tradeRepository.findByIdAndUserId(tradeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trade", "id", tradeId));
        return mapToResponse(trade);
    }

    private TradeResponse mapToResponse(Trade trade) {
        return TradeResponse.builder()
                .id(trade.getId())
                .userId(trade.getUserId())
                .symbol(trade.getSymbol())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .tradeType(trade.getTradeType().name())
                .status(trade.getStatus().name())
                .totalValue(trade.getTotalValue())
                .executedAt(trade.getExecutedAt())
                .build();
    }
}
