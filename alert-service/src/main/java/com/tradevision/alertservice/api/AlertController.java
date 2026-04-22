package com.tradevision.alertservice.api;

import com.tradevision.common.dto.AlertDto;
import com.tradevision.common.dto.ApiResponse;
import com.tradevision.alertservice.model.AlertType;
import com.tradevision.alertservice.service.AlertService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<ApiResponse<AlertDto>> createAlert(
            @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        AlertDto alert = alertService.createAlert(userId, request.getSymbol(),
                request.getAlertType(), request.getTargetPrice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert created", alert));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertDto>>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<AlertDto> alerts = activeOnly
                ? alertService.getActiveAlerts(userId)
                : alertService.getAlerts(userId);
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        alertService.deleteAlert(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Alert deleted", null));
    }

    @Data
    public static class CreateAlertRequest {
        @NotBlank
        private String symbol;
        @NotNull
        private AlertType alertType;
        @NotNull
        @DecimalMin("0.0001")
        private BigDecimal targetPrice;
    }
}
