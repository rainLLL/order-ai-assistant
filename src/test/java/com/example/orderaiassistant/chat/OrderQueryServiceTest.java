package com.example.orderaiassistant.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ivy
 * @date 2026/10/2
 * @description
 */
class OrderQueryServiceTest {


    @Test
    void ownerCanReadOrder() {
        // 准备：全新的模拟订单服务
        var service = new OrderQueryService();

        // 执行：1001 查询自己的订单
        var result = service.query("O1001", 1001L);

        // 检查
        assertEquals("SUCCESS", result.code());
        assertNotNull(result.order());
        assertEquals("O1001", result.order().orderId());
        assertEquals("PROCESSING", result.order().refundStatus());
    }

    @Test
    void otherUserCannotReadOrder() {
        var service = new OrderQueryService();

        // O1002 属于 1002，当前查询者是 1001
        var result = service.query("O1002", 1001L);

        assertEquals("NOT_FOUND_OR_FORBIDDEN", result.code());
        assertNull(result.order());
    }
}