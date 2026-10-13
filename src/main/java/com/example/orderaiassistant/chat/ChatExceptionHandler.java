package com.example.orderaiassistant.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.UUID;

/**
 * @author Ivy
 * @date 2026/9/24
 * @description
 */
@RestControllerAdvice(assignableTypes = ChatController.class)
public class ChatExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ChatExceptionHandler.class);

    public record ErrorResponse(
            String requestId,
            String code,
            String message
    ) {
    }

    @ExceptionHandler({
            RestClientException.class,
            TransientAiException.class,
            NonTransientAiException.class
    })
    public ResponseEntity<ErrorResponse> handleModelError(Exception ex) {
        String requestId = MDC.get("requestId");
        boolean timeout = isTimeout(ex);

        HttpStatus status = timeout
                ? HttpStatus.GATEWAY_TIMEOUT
                : HttpStatus.BAD_GATEWAY;

        String code = timeout
                ? "MODEL_TIMEOUT"
                : "MODEL_CALL_FAILED";

        String message = timeout
                ? "模型响应超时，请稍后再试。"
                : "模型调用失败，请稍后再试。";

        log.warn(
                "requestId={} operation=model.generate code={} exceptionType={}",
                requestId,
                code,
                ex.getClass().getSimpleName(),
                ex
        );

        return ResponseEntity.status(status)
                .body(new ErrorResponse(requestId, code, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String requestId = MDC.get("requestId");

        var fieldError = ex.getBindingResult().getFieldError();

        String message = "请求参数不符合要求";
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getDefaultMessage();
        }

        log.info(
                "requestId={} code=INVALID_INPUT field={}",
                requestId,
                fieldError == null ? "unknown" : fieldError.getField()
        );

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        requestId,
                        "INVALID_INPUT",
                        message
                ));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex) {

        String requestId = MDC.get("requestId");

        log.info(
                "requestId={} code=INVALID_REQUEST_BODY",
                requestId
        );

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        requestId,
                        "INVALID_REQUEST_BODY",
                        "请求体缺失或格式不正确，请提交符合接口要求的JSON。"
                ));
    }

    private boolean isTimeout(Throwable error) {
        Throwable current = error;

        for (int i = 0; current != null && i < 20; i++) {
            if (current instanceof SocketTimeoutException
                    || current instanceof HttpTimeoutException
                    || current instanceof io.netty.handler.timeout.ReadTimeoutException) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
}
