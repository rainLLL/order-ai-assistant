package com.example.orderaiassistant.refund;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * @author Ivy
 * @date 2026/9/29
 * @description
 */
@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;
    private final long currentUserId;

    public RefundController(
            RefundService refundService,
            @Value("${demo.current-user-id}") long currentUserId) {
        this.refundService = refundService;
        this.currentUserId = currentUserId;
    }

    public record PrepareRequest(@NotBlank String orderId) {
    }

    @PostMapping("/prepare")
    public RefundService.Refund prepare(
            @Valid @RequestBody PrepareRequest request) {
        return refundService.prepare(request.orderId(), currentUserId);
    }

    @PostMapping("/{id}/confirm")
    public RefundService.Refund confirm(@PathVariable("id") String id) {
        return refundService.confirm(id, currentUserId);
    }

    @GetMapping("/{id}")
    public RefundService.Refund query(@PathVariable("id") String id) {
        return refundService.query(id, currentUserId);
    }

    @PostMapping("/{id}/reconcile")
    public RefundService.Refund reconcile(@PathVariable("id") String id) {
        return refundService.reconcile(id, currentUserId);
    }
}