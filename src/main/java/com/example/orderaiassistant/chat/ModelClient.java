package com.example.orderaiassistant.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.ToolCallLimitExceededException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Ivy
 * @date 2026/9/24
 * @description
 */
@Component
public class ModelClient {

    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public ModelClient(ChatClient.Builder builder,
                       OrderTools orderTools) {
        this.orderTools = orderTools;
        this.chatClient = builder
                .defaultOptions(DeepSeekChatOptions.builder()
                        .thinking(DeepSeekApi.ChatCompletionRequest.Thinking.DISABLED))
                .build();
    }

    public String generate(String instruction, String userMessage) {
        var response = chatClient.prompt()
                .system(instruction)
                .user(userMessage)
                .tools(orderTools)
                .call()
                .chatResponse()
                ;

        if (response == null || response.getResult() == null) {
            throw new IllegalStateException("模型未返回有效结果");
        }

        var result = response.getResult();

        String finishReason = result.getMetadata().getFinishReason();

        if (ToolCallLimitExceededException.FINISH_REASON
                .equals(finishReason)) {
            return "本次任务已达到工具调用次数上限，查询已停止。"
                    + "停止查询不代表退款成功或失败。";
        }

        return result.getOutput().getText();
    }
}
