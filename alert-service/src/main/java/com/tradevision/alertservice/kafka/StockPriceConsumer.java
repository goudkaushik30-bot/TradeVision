package com.tradevision.alertservice.kafka;

import com.tradevision.common.event.StockPriceEvent;
import com.tradevision.alertservice.service.AlertService;
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
public class StockPriceConsumer {

    private final AlertService alertService;

    @KafkaListener(
            topics = "stock-price-events",
            groupId = "alert-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStockPriceEvent(
            @Payload StockPriceEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        log.debug("Received stock price event: symbol={}, price={} from partition={}, offset={}",
                event.getSymbol(), event.getCurrentPrice(), partition, offset);

        try {
            alertService.checkAndTriggerAlerts(event.getSymbol(), event.getCurrentPrice());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing stock price event for {}: {}",
                    event.getSymbol(), e.getMessage(), e);
        }
    }
}
