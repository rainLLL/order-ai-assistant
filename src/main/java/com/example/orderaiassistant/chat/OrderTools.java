package com.example.orderaiassistant.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Ivy
 * @date 2026/9/24
 * @description
 */
@Component
public class OrderTools {

    private final OrderQueryService orderQueryService;
    private final long currentUserId;

    public OrderTools(
            OrderQueryService orderQueryService,
            @Value("${demo.current-user-id}") long currentUserId) {

        this.orderQueryService = orderQueryService;
        this.currentUserId = currentUserId;
    }

    @Tool(description = """
            根据订单编号查询当前模拟用户有权访问的订单。
            仅查询，不执行退款。
            code不是SUCCESS时，不得推断订单支付或退款状态。
            遇到 NOT_FOUND_OR_FORBIDDEN 时，只说明订单不存在或无权访问。
            即使用户确认编号正确，也不能断定订单存在或确定是权限问题。
            不要编造其他订单编号作为建议。
            """)
    public OrderQueryService.QueryResult getOrderStatus(
            @ToolParam(description = "订单编号，例如 O1001")
            String orderId) {
        System.out.println("实际执行订单查询：" + orderId);
        return orderQueryService.query(orderId, currentUserId);
    }
}