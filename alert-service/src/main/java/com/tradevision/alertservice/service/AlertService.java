package com.tradevision.alertservice.service;

import com.tradevision.common.dto.AlertDto;
import com.tradevision.common.exception.ResourceNotFoundException;
import com.tradevision.alertservice.model.Alert;
import com.tradevision.alertservice.model.AlertType;
import com.tradevision.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional
    public AlertDto createAlert(Long userId, String symbol, AlertType alertType,
                                 BigDecimal targetPrice) {
        Alert alert = Alert.builder()
                .userId(userId)
                .symbol(symbol.toUpperCase())
                .alertType(alertType)
                .targetPrice(targetPrice)
                .active(true)
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Created alert {} for user {} on {} ({} {})",
                saved.getId(), userId, symbol, alertType, targetPrice);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AlertDto> getAlerts(Long userId) {
        return alertRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertDto> getActiveAlerts(Long userId) {
        return alertRepository.findByUserIdAndActive(userId, true).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAlert(Long alertId, Long userId) {
        Alert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));
        alertRepository.delete(alert);
        log.info("Deleted alert {} for user {}", alertId, userId);
    }

    @Transactional
    public void checkAndTriggerAlerts(String symbol, BigDecimal currentPrice) {
        List<Alert> activeAlerts = alertRepository.findBySymbolAndActive(symbol, true);

        for (Alert alert : activeAlerts) {
            boolean triggered = false;

            if (alert.getAlertType() == AlertType.PRICE_ABOVE
                    && currentPrice.compareTo(alert.getTargetPrice()) >= 0) {
                triggered = true;
            } else if (alert.getAlertType() == AlertType.PRICE_BELOW
                    && currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                triggered = true;
            }

            if (triggered) {
                alert.setActive(false);
                alert.setTriggeredAt(LocalDateTime.now());
                alertRepository.save(alert);
                log.info("Alert {} triggered for user {} on {} at price {}",
                        alert.getId(), alert.getUserId(), symbol, currentPrice);
                // In production, send notification (email, push, websocket)
            }
        }
    }

    public AlertDto mapToDto(Alert alert) {
        return AlertDto.builder()
                .id(alert.getId())
                .userId(alert.getUserId())
                .symbol(alert.getSymbol())
                .alertType(alert.getAlertType().name())
                .targetPrice(alert.getTargetPrice())
                .active(alert.isActive())
                .triggeredAt(alert.getTriggeredAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
