package com.tradevision.tradeservice.api;

import com.tradevision.common.dto.ApiResponse;
import com.tradevision.tradeservice.dto.TradeRequest;
import com.tradevision.tradeservice.dto.TradeResponse;
import com.tradevision.tradeservice.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TradeController {

    private final TradeService tradeService;

    @PostMapping
    public ResponseEntity<ApiResponse<TradeResponse>> executeTrade(
            @Valid @RequestBody TradeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TradeResponse response = tradeService.executeTrade(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trade executed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TradeResponse>>> getTradeHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(tradeService.getTradeHistory(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TradeResponse>> getTradeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(tradeService.getTradeById(id, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<TradeResponse>> cancelTrade(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TradeResponse response = tradeService.cancelTrade(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Trade cancelled", response));
    }
}
