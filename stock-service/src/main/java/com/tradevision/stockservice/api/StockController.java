package com.tradevision.stockservice.api;

import com.tradevision.common.dto.ApiResponse;
import com.tradevision.common.dto.StockDto;
import com.tradevision.stockservice.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDto>>> getAllStocks() {
        return ResponseEntity.ok(ApiResponse.success(stockService.getAllStocks()));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<StockDto>> getStockBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getStockBySymbol(symbol)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockDto>>> searchStocks(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(stockService.searchStocks(q)));
    }

    @GetMapping("/watchlist")
    public ResponseEntity<ApiResponse<List<StockDto>>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(stockService.getWatchlist(userId)));
    }

    @PostMapping("/watchlist")
    public ResponseEntity<ApiResponse<Void>> addToWatchlist(
            @RequestParam String symbol,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        stockService.addToWatchlist(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success("Stock added to watchlist", null));
    }

    @DeleteMapping("/watchlist/{symbol}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable String symbol,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        stockService.removeFromWatchlist(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success("Stock removed from watchlist", null));
    }

    private Long extractUserId(UserDetails userDetails) {
        // userId is stored as username in JWT for inter-service calls;
        // in production this would be resolved via a user lookup or JWT claim
        return Long.parseLong(userDetails.getUsername());
    }
}
