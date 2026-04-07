package com.tradevision.stockservice.kafka;

import com.tradevision.common.event.StockPriceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceProducer {

    private static final String TOPIC = "stock-price-events";

    private final KafkaTemplate<String, StockPriceEvent> kafkaTemplate;

    public void publishPriceUpdate(String symbol, BigDecimal previousPrice,
                                   BigDecimal currentPrice, BigDecimal changePercent) {
        StockPriceEvent event = StockPriceEvent.builder()
                .symbol(symbol)
                .previousPrice(previousPrice)
                .currentPrice(currentPrice)
                .changePercent(changePercent)
                .build();

        CompletableFuture<SendResult<String, StockPriceEvent>> future =
                kafkaTemplate.send(TOPIC, symbol, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish stock price event for {}: {}", symbol, ex.getMessage());
            } else {
                log.debug("Published stock price event for {} to partition {}",
                        symbol, result.getRecordMetadata().partition());
            }
        });
    }
}
