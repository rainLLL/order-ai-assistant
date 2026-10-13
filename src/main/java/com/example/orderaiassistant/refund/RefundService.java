package com.example.orderaiassistant.refund;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ivy
 * @date 2026/9/29
 * @description
 */
@Service
public class RefundService {

    public enum Status {
        PENDING_CONFIRMATION, PROCESSING, SUCCESS, CANCELLED, UNKNOWN
    }

    public record Refund(
            String refundRequestId,
            long userId,
            String orderId,
            BigDecimal amount,
            String currency,
            Status status
    ) {
        public Refund withStatus(Status newStatus) {
            return new Refund(
                    refundRequestId, userId, orderId,
                    amount, currency, newStatus
            );
        }
    }
    private static class MockChannelTimeoutException
            extends RuntimeException {
    }

    // 本节专用模拟订单：已支付，且属于用户1001
    private static final String ORDER_ID = "O1003";
    private static final long OWNER_ID = 1001L;
    private static final BigDecimal PAID = new BigDecimal("88.00");

    private BigDecimal refunded = BigDecimal.ZERO;

    private final Map<String, Refund> refunds = new HashMap<>();

    // 模拟支付渠道按幂等键保存结果
    private final Map<String, String> channelResults = new HashMap<>();
    private int channelExecutions = 0;

    public synchronized Refund prepare(String orderId, long userId) {
        if (!ORDER_ID.equals(orderId) || userId != OWNER_ID) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "订单不存在或无权访问"
            );
        }

        // 本节仅支持该订单的一笔全额退款，重复准备返回已有申请
        for (Refund existing : refunds.values()) {
            if (existing.orderId().equals(orderId)
                    && existing.status() != Status.CANCELLED) {
                return existing;
            }
        }

        BigDecimal remaining = PAID.subtract(refunded);
        if (remaining.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "订单没有剩余可退金额"
            );
        }

        Refund refund = new Refund(
                UUID.randomUUID().toString(),
                userId,
                orderId,
                remaining,
                "CNY",
                Status.PENDING_CONFIRMATION
        );

        refunds.put(refund.refundRequestId(), refund);
        return refund;
    }

    public synchronized Refund confirm(String id, long userId) {
        Refund refund = query(id, userId);

        // 已成功、处理中或结果未知，都不直接再次提交
        if (refund.status() == Status.SUCCESS
                || refund.status() == Status.PROCESSING
                || refund.status() == Status.UNKNOWN) {
            return refund;
        }

        if (refund.status() != Status.PENDING_CONFIRMATION) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "当前状态不能执行确认"
            );
        }

        BigDecimal remaining = PAID.subtract(refunded);

        if (remaining.signum() <= 0
                || refund.amount().compareTo(remaining) != 0) {
            refunds.put(id, refund.withStatus(Status.CANCELLED));
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "可退金额已变化，请重新准备申请"
            );
        }

        refunds.put(id, refund.withStatus(Status.PROCESSING));

        try {
            callMockChannel(id, refund.amount());
        } catch (MockChannelTimeoutException ex) {
            Refund unknown = refund.withStatus(Status.UNKNOWN);
            refunds.put(id, unknown);
            return unknown;
        }

        return completeLocally(refund);
    }
    private Refund completeLocally(Refund refund) {
        Refund current = refunds.get(refund.refundRequestId());

        if (current.status() == Status.SUCCESS) {
            return current;
        }

        refunded = refunded.add(current.amount());

        Refund success = current.withStatus(Status.SUCCESS);
        refunds.put(current.refundRequestId(), success);

        System.out.println("本地累计已退款金额：" + refunded);
        return success;
    }

    public synchronized Refund reconcile(String id, long userId) {
        Refund refund = query(id, userId);

        if (refund.status() == Status.SUCCESS) {
            return refund;
        }

        if (refund.status() != Status.UNKNOWN
                && refund.status() != Status.PROCESSING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "当前状态无需执行结果核对"
            );
        }

        // 查询模拟渠道已有记录，不执行退款
        String channelStatus = channelResults.get(id);

        if ("SUCCESS".equals(channelStatus)) {
            return completeLocally(refund);
        }

        // 查不到结果，不直接认定没有执行
        Refund unknown = refund.withStatus(Status.UNKNOWN);
        refunds.put(id, unknown);
        return unknown;
    }

    public synchronized Refund query(String id, long userId) {
        Refund refund = refunds.get(id);

        if (refund == null || refund.userId() != userId) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "退款申请不存在或无权访问"
            );
        }

        return refund;
    }

    private void callMockChannel(String id, BigDecimal amount) {
        if (channelResults.containsKey(id)) {
            return;
        }

        channelExecutions++;

        // 模拟渠道已经执行成功，并保存自己的结果
        channelResults.put(id, "SUCCESS");

        System.out.printf(
                "渠道已退款：id=%s，amount=%s，累计执行次数=%d%n",
                id, amount, channelExecutions
        );

        // 模拟成功响应未送达调用方
        throw new MockChannelTimeoutException();
    }
    synchronized int channelExecutionCount() {
        return channelExecutions;
    }

    synchronized BigDecimal totalRefundedAmount() {
        return refunded;
    }
}
