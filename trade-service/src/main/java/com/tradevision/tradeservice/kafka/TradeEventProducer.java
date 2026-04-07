package com.tradevision.tradeservice.kafka;

import com.tradevision.common.event.TradeEvent;
import com.tradevision.tradeservice.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeEventProducer {

    private static final String TOPIC = "trade-events";

    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;

    public void publishTradeEvent(Trade trade) {
        TradeEvent event = TradeEvent.builder()
                .tradeId(trade.getId().toString())
                .userId(trade.getUserId().toString())
                .symbol(trade.getSymbol())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .tradeType(trade.getTradeType().name())
                .status(trade.getStatus().name())
                .build();

        CompletableFuture<SendResult<String, TradeEvent>> future =
                kafkaTemplate.send(TOPIC, trade.getUserId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish trade event for trade {}: {}",
                        trade.getId(), ex.getMessage());
            } else {
                log.debug("Published trade event for trade {} to partition {}",
                        trade.getId(), result.getRecordMetadata().partition());
            }
        });
    }
}
