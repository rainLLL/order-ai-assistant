package com.example.orderaiassistant.chat;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ivy
 * @date 2026/9/27
 * @description
 */
@Service
public class OrderQueryService {

    private record Order(
            String orderId,
            long ownerId,
            String paymentStatus,
            String refundStatus
    ) {
    }

    public record OrderView(
            String orderId,
            String paymentStatus,
            String refundStatus
    ) {
    }

    public record QueryResult(
            String code,
            OrderView order,
            String message
    ) {
    }

    private final Map<String, Order> orders = Map.of(
            "O1001", new Order("O1001", 1001L, "PAID", "PROCESSING"),
            "O1002", new Order("O1002", 1002L, "PAID", "SUCCESS")
    );

    public QueryResult query(String orderId, long currentUserId) {
        if (orderId == null || orderId.isBlank()) {
            return new QueryResult(
                    "INVALID_INPUT", null, "订单编号不能为空"
            );
        }

        Order order = orders.get(orderId.strip());

        if (order == null || order.ownerId() != currentUserId) {
            return new QueryResult(
                    "NOT_FOUND_OR_FORBIDDEN",
                    null,
                    "订单不存在或当前用户无权访问"
            );
        }

        OrderView view = new OrderView(
                order.orderId(),
                order.paymentStatus(),
                order.refundStatus()
        );

        return new QueryResult(
                "SUCCESS", view, "查询成功，数据来源为本地模拟"
        );
    }
}
