package com.example.orderaiassistant.chat;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ivy
 * @date 2026/9/27
 * @description
 */
@Service
public class PolicyRetrievalService {

    public record Policy(
            String id,
            String title,
            String content,
            List<String> keywords
    ) {
    }

    private final List<Policy> policies = List.of(
            new Policy(
                    "P01",
                    "退款到账时间",
                    "支付渠道确认退款成功后，预计5至7个工作日到账。"
                            + "该时间不从用户提交申请时开始计算。",
                    List.of("到账", "多久", "几天")
            ),
            new Policy(
                    "P02",
                    "退款处理中",
                    "退款状态为PROCESSING时，应继续查询处理进度。"
                            + "不能将处理中解释为退款成功或退款失败，"
                            + "也不应仅因尚未完成而重复提交退款。",
                    List.of("处理中", "PROCESSING", "进度", "重复")
            ),
            new Policy(
                    "P03",
                    "修改收货地址",
                    "未发货订单可申请修改收货地址；"
                            + "已发货订单需联系人工客服核实。",
                    List.of("地址", "收货", "发货")
            )
    );

    public List<Policy> search(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }

        return policies.stream()
                .filter(policy -> policy.keywords().stream()
                        .anyMatch(question::contains))
                .toList();
    }
}
