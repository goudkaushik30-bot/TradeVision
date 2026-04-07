package com.tradevision.portfolioservice.kafka;

import com.tradevision.common.event.TradeEvent;
import com.tradevision.portfolioservice.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventConsumer {

    private final PortfolioService portfolioService;

    @KafkaListener(
            topics = "trade-events",
            groupId = "portfolio-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTradeEvent(
            @Payload TradeEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        log.info("Received trade event: tradeId={}, userId={}, symbol={}, type={} from partition={}, offset={}",
                event.getTradeId(), event.getUserId(), event.getSymbol(),
                event.getTradeType(), partition, offset);

        try {
            if ("EXECUTED".equals(event.getStatus())) {
                portfolioService.updatePositionFromTradeEvent(event);
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing trade event {}: {}", event.getTradeId(), e.getMessage(), e);
            // In production, implement dead-letter queue handling here
        }
    }
}
