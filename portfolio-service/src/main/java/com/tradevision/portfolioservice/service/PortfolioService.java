package com.tradevision.portfolioservice.service;

import com.tradevision.common.dto.PortfolioDto;
import com.tradevision.common.event.TradeEvent;
import com.tradevision.common.exception.ResourceNotFoundException;
import com.tradevision.portfolioservice.model.Portfolio;
import com.tradevision.portfolioservice.model.PortfolioSummary;
import com.tradevision.portfolioservice.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Cacheable(value = "portfolio", key = "#userId")
    @Transactional(readOnly = true)
    public List<PortfolioDto> getPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortfolioSummary getPortfolioSummary(Long userId) {
        List<Portfolio> positions = portfolioRepository.findByUserId(userId);

        BigDecimal totalValue = positions.stream()
                .map(p -> p.getCurrentValue() != null ? p.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = positions.stream()
                .map(p -> p.getAvgBuyPrice().multiply(p.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitLoss = totalValue.subtract(totalCost);
        BigDecimal totalPnLPercent = totalCost.compareTo(BigDecimal.ZERO) != 0
                ? totalProfitLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return PortfolioSummary.builder()
                .userId(userId)
                .totalValue(totalValue)
                .totalCost(totalCost)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercent(totalPnLPercent)
                .positionCount(positions.size())
                .positions(positions)
                .build();
    }

    @Transactional(readOnly = true)
    public PortfolioDto getPositionBySymbol(Long userId, String symbol) {
        Portfolio portfolio = portfolioRepository.findByUserIdAndSymbol(userId, symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio position", "symbol", symbol));
        return mapToDto(portfolio);
    }

    @CacheEvict(value = "portfolio", key = "#event.userId")
    @Transactional
    public void updatePositionFromTradeEvent(TradeEvent event) {
        Long userId = Long.parseLong(event.getUserId());
        String symbol = event.getSymbol();

        if ("BUY".equals(event.getTradeType())) {
            handleBuy(userId, symbol, event.getQuantity(), event.getPrice());
        } else if ("SELL".equals(event.getTradeType())) {
            handleSell(userId, symbol, event.getQuantity(), event.getPrice());
        }
    }

    private void handleBuy(Long userId, String symbol, BigDecimal quantity, BigDecimal price) {
        portfolioRepository.findByUserIdAndSymbol(userId, symbol)
                .ifPresentOrElse(
                        position -> {
                            BigDecimal totalCost = position.getAvgBuyPrice()
                                    .multiply(position.getQuantity())
                                    .add(price.multiply(quantity));
                            BigDecimal newQuantity = position.getQuantity().add(quantity);
                            BigDecimal newAvgPrice = totalCost.divide(newQuantity, 4, RoundingMode.HALF_UP);

                            position.setQuantity(newQuantity);
                            position.setAvgBuyPrice(newAvgPrice);
                            recalculate(position, price);
                            portfolioRepository.save(position);
                            log.info("Updated BUY position for user {} symbol {}", userId, symbol);
                        },
                        () -> {
                            Portfolio newPosition = Portfolio.builder()
                                    .userId(userId)
                                    .symbol(symbol)
                                    .quantity(quantity)
                                    .avgBuyPrice(price)
                                    .currentPrice(price)
                                    .build();
                            recalculate(newPosition, price);
                            portfolioRepository.save(newPosition);
                            log.info("Created new position for user {} symbol {}", userId, symbol);
                        }
                );
    }

    private void handleSell(Long userId, String symbol, BigDecimal quantity, BigDecimal price) {
        portfolioRepository.findByUserIdAndSymbol(userId, symbol)
                .ifPresent(position -> {
                    BigDecimal newQuantity = position.getQuantity().subtract(quantity);
                    if (newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                        portfolioRepository.delete(position);
                        log.info("Deleted position for user {} symbol {} (fully sold)", userId, symbol);
                    } else {
                        position.setQuantity(newQuantity);
                        recalculate(position, price);
                        portfolioRepository.save(position);
                        log.info("Updated SELL position for user {} symbol {}", userId, symbol);
                    }
                });
    }

    private void recalculate(Portfolio position, BigDecimal currentPrice) {
        position.setCurrentPrice(currentPrice);
        BigDecimal currentValue = currentPrice.multiply(position.getQuantity());
        BigDecimal cost = position.getAvgBuyPrice().multiply(position.getQuantity());
        BigDecimal pnl = currentValue.subtract(cost);
        BigDecimal pnlPercent = cost.compareTo(BigDecimal.ZERO) != 0
                ? pnl.divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        position.setCurrentValue(currentValue);
        position.setProfitLoss(pnl);
        position.setProfitLossPercent(pnlPercent);
    }

    public PortfolioDto mapToDto(Portfolio p) {
        return PortfolioDto.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .symbol(p.getSymbol())
                .quantity(p.getQuantity())
                .avgBuyPrice(p.getAvgBuyPrice())
                .currentValue(p.getCurrentValue())
                .profitLoss(p.getProfitLoss())
                .profitLossPercent(p.getProfitLossPercent())
                .build();
    }
}
