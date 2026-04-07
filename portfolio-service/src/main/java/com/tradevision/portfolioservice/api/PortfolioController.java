package com.tradevision.portfolioservice.api;

import com.tradevision.common.dto.ApiResponse;
import com.tradevision.common.dto.PortfolioDto;
import com.tradevision.portfolioservice.model.PortfolioSummary;
import com.tradevision.portfolioservice.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PortfolioDto>>> getPortfolio(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPortfolio(userId)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PortfolioSummary>> getPortfolioSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPortfolioSummary(userId)));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<PortfolioDto>> getPositionBySymbol(
            @PathVariable String symbol,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(
                portfolioService.getPositionBySymbol(userId, symbol)));
    }
}
