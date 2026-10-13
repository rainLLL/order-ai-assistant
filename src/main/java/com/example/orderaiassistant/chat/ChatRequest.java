package com.example.orderaiassistant.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author Ivy
 * @date 2026/9/24
 * @description
 */
public record ChatRequest(
        @NotBlank(message = "消息不能为空")
        @Size(max = 2000, message = "消息不能超过2000个字符")
        String message
) {
}
