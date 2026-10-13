package com.example.orderaiassistant.chat;

import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Ivy
 * @date 2026/9/24
 * @description
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String requestId = MDC.get("requestId");
        String answer = chatService.reply(request.message());

        System.out.println(">>>answer>>>");
        System.out.println(answer);
        System.out.println(">>>end>>>");

        return new ChatResponse(requestId, answer);
    }
}
