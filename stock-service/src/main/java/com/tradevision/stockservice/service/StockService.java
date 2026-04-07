package com.tradevision.stockservice.service;

import com.tradevision.common.dto.StockDto;
import com.tradevision.common.exception.ResourceNotFoundException;
import com.tradevision.common.exception.TradeVisionException;
import com.tradevision.stockservice.kafka.StockPriceProducer;
import com.tradevision.stockservice.model.Stock;
import com.tradevision.stockservice.model.WatchlistItem;
import com.tradevision.stockservice.repository.StockRepository;
import com.tradevision.stockservice.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final WatchlistRepository watchlistRepository;
    private final StockPriceProducer stockPriceProducer;

    @Cacheable(value = "stocks", key = "'all'")
    @Transactional(readOnly = true)
    public List<StockDto> getAllStocks() {
        return stockRepository.findAllByOrderBySymbolAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "stocks", key = "#symbol")
    @Transactional(readOnly = true)
    public StockDto getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol));
        return mapToDto(stock);
    }

    @Transactional(readOnly = true)
    public List<StockDto> searchStocks(String query) {
        return stockRepository.searchBySymbolOrCompanyName(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "stocks", allEntries = true)
    @Transactional
    public StockDto updateStockPrice(String symbol, BigDecimal newPrice) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol));

        BigDecimal oldPrice = stock.getCurrentPrice();
        BigDecimal change = newPrice.subtract(oldPrice);
        BigDecimal changePercent = oldPrice.compareTo(BigDecimal.ZERO) != 0
                ? change.divide(oldPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        stock.setPreviousClose(oldPrice);
        stock.setCurrentPrice(newPrice);
        stock.setChange(change);
        stock.setChangePercent(changePercent);

        Stock saved = stockRepository.save(stock);
        stockPriceProducer.publishPriceUpdate(symbol, oldPrice, newPrice, changePercent);
        log.info("Updated stock price for {}: {} -> {}", symbol, oldPrice, newPrice);
        return mapToDto(saved);
    }

    @Transactional
    public void addToWatchlist(Long userId, String symbol) {
        if (!stockRepository.existsBySymbol(symbol.toUpperCase())) {
            throw new ResourceNotFoundException("Stock", "symbol", symbol);
        }
        if (watchlistRepository.existsByUserIdAndStockSymbol(userId, symbol.toUpperCase())) {
            throw new TradeVisionException("Stock already in watchlist", HttpStatus.CONFLICT);
        }
        WatchlistItem item = WatchlistItem.builder()
                .userId(userId)
                .stockSymbol(symbol.toUpperCase())
                .build();
        watchlistRepository.save(item);
        log.info("Added {} to watchlist for user {}", symbol, userId);
    }

    @Transactional(readOnly = true)
    public List<StockDto> getWatchlist(Long userId) {
        List<WatchlistItem> items = watchlistRepository.findByUserId(userId);
        return items.stream()
                .map(item -> stockRepository.findBySymbol(item.getStockSymbol()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> mapToDto(opt.get()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeFromWatchlist(Long userId, String symbol) {
        if (!watchlistRepository.existsByUserIdAndStockSymbol(userId, symbol.toUpperCase())) {
            throw new ResourceNotFoundException("Watchlist item", "symbol", symbol);
        }
        watchlistRepository.deleteByUserIdAndStockSymbol(userId, symbol.toUpperCase());
        log.info("Removed {} from watchlist for user {}", symbol, userId);
    }

    public StockDto mapToDto(Stock stock) {
        return StockDto.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .change(stock.getChange())
                .changePercent(stock.getChangePercent())
                .volume(stock.getVolume())
                .marketCap(stock.getMarketCap())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }
}
