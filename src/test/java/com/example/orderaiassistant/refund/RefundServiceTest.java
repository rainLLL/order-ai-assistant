package com.example.orderaiassistant.refund;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ivy
 * @date 2026/10/2
 * @description
 */
class RefundServiceTest {

    @Test
    void repeatedConfirmationAndReconciliationDoNotDuplicateRefund() {
        // 每次测试都从新的内存数据开始
        var service = new RefundService();

        // 1. 准备申请，不应实际退款
        var prepared = service.prepare("O1003", 1001L);
        String id = prepared.refundRequestId();

        assertEquals(
                RefundService.Status.PENDING_CONFIRMATION,
                prepared.status()
        );
        assertEquals(0, service.channelExecutionCount());

        // 2. 渠道成功，但模拟响应丢失，本地成为 UNKNOWN
        var first = service.confirm(id, 1001L);

        assertEquals(RefundService.Status.UNKNOWN, first.status());
        assertEquals(1, service.channelExecutionCount());

        // 此时本地尚未确认成功，累计金额仍为 0
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(service.totalRefundedAmount())
        );

        // 3. 重复确认，不得再次调用渠道退款
        var second = service.confirm(id, 1001L);

        assertEquals(RefundService.Status.UNKNOWN, second.status());
        assertEquals(1, service.channelExecutionCount());

        // 4. 对账后，本地恢复成功
        var reconciled = service.reconcile(id, 1001L);

        assertEquals(RefundService.Status.SUCCESS, reconciled.status());
        assertEquals(
                0,
                new BigDecimal("88.00")
                        .compareTo(service.totalRefundedAmount())
        );

        // 5. 再次对账，不得重复退款或重复累计金额
        var repeated = service.reconcile(id, 1001L);

        assertEquals(RefundService.Status.SUCCESS, repeated.status());
        assertEquals(1, service.channelExecutionCount());
        assertEquals(
                0,
                new BigDecimal("88.00")
                        .compareTo(service.totalRefundedAmount())
        );
    }
}