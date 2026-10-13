# Order AI Assistant

基于 Spring Boot 和 Spring AI 的订单问答教学项目，
使用 DeepSeek 完成自然语言交互。

## 已实现功能

- 聊天输入校验
- 模型调用与统一模型异常响应
- 订单查询工具及订单所属权校验
- 基于本地关键词检索的政策问答
- 模拟退款申请、确认、幂等与 UNKNOWN 对账恢复
- 工具调用次数限制
- 请求编号关联日志、HTTP 状态及请求耗时记录
- Service 层自动化回归测试

## 运行环境

- Java：填写实际编译与运行版本
- Maven：用于测试和打包
- DeepSeek API Key：通过 DEEPSEEK_API_KEY 环境变量提供

## 测试与打包

执行：

mvn clean package

如果没有单独安装 Maven，可以在 IDEA 的 Maven 窗口中执行：
clean package

## 启动

在当前终端中设置 DEEPSEEK_API_KEY，然后执行：

java -jar target/order-ai-assistant-0.0.1-SNAPSHOT.jar

JAR 文件名以实际打包产物为准。
默认端口为 8080。

## 示例请求

POST /api/chat
Content-Type: application/json

{
"message": "查询 O1001 的退款状态"
}

## 教学数据

- 当前用户由服务端 demo.current-user-id 配置模拟
- O1001 属于用户 1001，退款状态为 PROCESSING
- O1002 属于用户 1002
- 退款流程使用 O1003 作为模拟订单
- 所有订单、政策和支付渠道数据均为教学模拟

## 验收记录

详细用例与执行证据见 docs 目录中的 evaluation.md。

- Service 自动化测试：3 个通过
- JAR 独立启动：待验证
- 独立启动后的聊天接口验收：待验证

## 已知限制

- 模拟用户身份，不是真实登录认证
- 数据仅保存在内存中，重启后不会保留
- 幂等与状态更新仅在当前单实例模拟范围内验证
- 订单查询与退款流程使用不同的模拟数据，尚未统一同步
- 政策检索采用关键词匹配，未使用向量检索
- 未接入真实支付渠道
- 尚未实现整个 Agent 任务的硬性总超时与金额预算控制
- 请求日志尚未覆盖每次模型调用的 Token、成本和分段耗时
- 当前版本用于本地教学演示