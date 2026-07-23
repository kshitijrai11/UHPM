package com.ultrahpm.orderservice.saga;

import com.ultrahpm.orderservice.entity.Order;
import com.ultrahpm.orderservice.entity.OrderItem;
import com.ultrahpm.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SAGA Step 3: Confirm the order in the local database.
 * Compensation: Cancel the order.
 */
@Component
public class ConfirmOrderStep implements SagaStep<OrderSagaContext> {

    private static final Logger log = LoggerFactory.getLogger(ConfirmOrderStep.class);
    private final OrderRepository orderRepository;

    public ConfirmOrderStep(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderSagaContext execute(OrderSagaContext context) throws Exception {
        log.info("Confirming order in database for userId={}", context.getUserId());

        Order order = new Order();
        order.setUserId(context.getUserId());
        order.setStatus("PAID");
        order.setTotalAmount(context.getTotalAmount());
        order.setCurrency("USD");
        order.setSagaId(context.getSagaId());
        order.setShippingAddress(context.getShippingAddress());

        List<OrderItem> items = context.getItems().stream()
                .map(req -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProductId(req.getProductId());
                    item.setProductName(req.getProductName());
                    item.setQuantity(req.getQuantity());
                    item.setUnitPrice(req.getUnitPrice());
                    return item;
                })
                .collect(Collectors.toList());

        order.setItems(items);

        Order saved = orderRepository.save(order);
        context.setOrderId(saved.getId());

        log.info("Order confirmed. orderId={}", saved.getId());
        return context;
    }

    @Override
    public OrderSagaContext compensate(OrderSagaContext context) {
        log.warn("COMPENSATING: Cancelling order orderId={}", context.getOrderId());

        if (context.getOrderId() != null) {
            orderRepository.findById(context.getOrderId()).ifPresent(order -> {
                order.setStatus("CANCELLED");
                orderRepository.save(order);
            });
        }

        log.info("Order cancelled. orderId={}", context.getOrderId());
        return context;
    }

    @Override
    public String getName() {
        return "ConfirmOrder";
    }
}
