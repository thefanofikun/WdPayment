# 项目总览

## 1. 项目目标

这个工作空间当前正在建设一个 `ToB 法币支付平台`，目前阶段重点是三块：

- 通道网关
- 支付流程
- 路由与监控

当前产品形态：

- 前端是一个 `单页运营控制台`
- 后端是一个 `Spring Boot Maven 多模块工程`
- 数据库按模块拆成多个 MySQL schema

现在的阶段更偏向一个可快速联调、可尽快接真实通道、同时又保留企业级演进空间的 MVP。

## 2. 当前整体架构

### 2.1 高层请求链路

当前主链路如下：

1. 用户在 Vue 控制台上操作
2. 前端通过 Vite 代理调用后端 `/api/...` 接口
3. 后端接收统一格式的上游请求
4. 网关服务或支付服务处理业务
5. 如果需要调用通道，则由网关选择对应的通道适配器
6. 适配器将统一参数转换为下游通道自己的字段
7. 下游响应被标准化后返回给前端
8. 同时把监控、路由历史、网关审计、支付订单等数据落到 MySQL

### 2.2 后端模块划分

后端现在已经升级为真正的 Maven 多模块工程。

#### `platform-common`

作用：

- 公共返回对象
- 公共异常处理
- 公共 Web 配置
- 模块间共享接口
- 公共参考数据模型

代表内容：

- `ApiResponse`
- `ErrorResponse`
- `ApiExceptionHandler`
- `ChannelCatalogProvider`
- `ChannelExecutionRecorder`
- `PaymentReferenceLookup`

#### `platform-gateway`

作用：

- 对上游暴露统一网关接口
- 按 `channelCode` 进行通道路由
- 统一参数转下游参数
- 管理通道适配器
- 记录网关审计日志

当前覆盖能力：

- 客户 onboarding
- 创建虚拟账户
- 创建 beneficiary
- 发起 payout
- webhook 接收

#### `platform-payment`

作用：

- 负责出入金订单录入
- 负责 checker / L1 / L2 审批流
- 负责幂等与防重复
- 负责状态机流转
- 审批通过后调用 gateway
- 负责支付列表查询和运营动作

当前支持的动作：

- 创建订单
- checker approve / reject
- L1 approve / reject
- L2 approve / reject
- cancel
- mark completed
- mark failed
- retry failed submission

#### `platform-ops`

作用：

- 通道监控
- 路由推荐
- 路由历史持久化
- 通道指标快照持久化

当前路由规则：

- 先按 `successRate` 倒序
- 再按 `averageLatencyMs` 升序

#### `platform-app`

作用：

- 最终装配模块
- 唯一的 Spring Boot 启动模块
- 运行时配置所在位置

这意味着：

- 代码结构已经模块化
- 部署形态暂时仍然是一个后端进程

这是当前阶段刻意的权衡，目的是先保证启动简单、联调快、风险低。

## 3. 数据库设计

当前 MySQL 按模块拆成三个 schema：

- `gateway_db`
- `payment_db`
- `ops_db`

### 3.1 `gateway_db`

主要存：

- 网关审计日志
- 通道配置种子数据
- webhook 事件日志预留表

典型数据包括：

- 统一请求报文
- 下游转换后请求
- 下游原始响应
- 标准化结果

### 3.2 `payment_db`

主要存：

- 支付订单
- 审批记录
- 支付事件时间线

典型数据包括：

- 商户和客户上下文
- 幂等键
- 请求通道和实际路由通道
- 审批动作
- 状态机流转
- 网关提交结果

### 3.3 `ops_db`

主要存：

- 通道监控快照
- 路由历史

典型数据包括：

- 成功率
- 失败数
- 平均延迟
- 最后状态
- 最后消息
- 每次推荐的排名结果

## 4. 前端页面交互说明

前端不是多页面后台，而是一个 `单页控制台`。  
顶层入口文件是：

- `frontend/src/App.vue`

目前有三个工作台：

- `Channel Gateway`
- `Routing & Monitoring`
- `Payment Module`

页签切换不会改浏览器路由，只是在一个页面里切换不同的工作区组件。

### 4.1 App 外壳如何工作

`App.vue` 里维护了一个 `workspaceTab` 状态。

交互方式：

1. 用户点击页签按钮
2. `workspaceTab` 被修改
3. 对应组件被渲染：
   - `GatewayWorkspace`
   - `MonitoringWorkspace`
   - `PaymentWorkspace`

这种方式很适合当前 ToB 内部控制台场景，简单直接，联调效率高。

### 4.2 Gateway 工作台怎么交互

主文件：

- `frontend/src/components/GatewayWorkspace.vue`

#### 组件加载时

组件挂载后会先调用：

- `GET /api/catalog/channels`

然后加载：

- 当前支持的通道
- 当前支持的 payout type

如果还没选通道，会自动选第一个。

#### 页面布局

左侧负责：

- 选择通道
- 选择要执行的网关能力

当前支持的能力：

- onboarding
- virtual account
- beneficiary
- payout
- webhook ingest

中间区域是表单。

右侧是检查区：

- 请求预览
- 响应结果

#### 提交流程

点击 `Send Request` 时：

1. 前端把表单拼成统一格式 payload
2. payload 转成 JSON
3. 根据当前操作调用对应接口：
   - `POST /api/gateway/customers/onboarding`
   - `POST /api/gateway/virtual-accounts`
   - `POST /api/gateway/beneficiaries`
   - `POST /api/gateway/payouts`
   - `POST /api/gateway/webhooks/ingest`
4. 后端返回标准化后的网关执行结果
5. 前端把结果显示在右侧响应窗口

#### 这个页面的价值

这个工作台是目前验证以下能力最快的入口：

- 上游统一参数是否合理
- 下游字段映射是否正确
- 响应标准化是否合理
- SGB 当前是模拟返回还是实际映射调用

### 4.3 Routing & Monitoring 工作台怎么交互

主文件：

- `frontend/src/components/MonitoringWorkspace.vue`

这个工作台分三块：

- 路由推荐
- 路由历史
- 通道监控

#### 组件加载时

会并行加载：

- `GET /api/routing/recommendations`
- `GET /api/monitoring/channels`
- `GET /api/routing/history`

#### 路由推荐区

用户可以选择操作类型，比如：

- CUSTOMER_ONBOARDING
- VIRTUAL_ACCOUNT
- BENEFICIARY
- PAYOUT
- WEBHOOK

然后前端调用：

- `GET /api/routing/recommendations?operation=...`

页面展示：

- 排名
- 通道
- 成功率
- 平均延迟
- 总量
- 推荐原因

#### 路由历史区

页面会调用：

- `GET /api/routing/history?operation=...`

这里展示的是已经持久化的历史路由结果，不只是当前内存中的一份快照。

#### 通道监控区

页面会调用：

- `GET /api/monitoring/channels`

当前展示指标：

- success rate
- failure count
- average latency
- last status
- last message
- last updated time

这块就是现在的运维可视化入口。

### 4.4 Payment 工作台怎么交互

主文件：

- `frontend/src/components/PaymentWorkspace.vue`

这是目前最偏业务操作的一个工作台。

#### 组件加载时

会并行拉四类数据：

- `GET /api/payment/reference-data`
- `GET /api/payment/orders`
- `GET /api/catalog/channels`
- `GET /api/routing/recommendations?operation=...`

#### 参考数据如何使用

当前参考数据还是 mock 的，但逻辑上是在模拟未来从 CRM、主数据系统或者商户中心拉过来的数据：

- merchants
- customers
- beneficiaries
- source accounts

前端用这些数据生成下拉框和默认值。

#### 表单的动态行为

这里有两个关键联动：

1. 当 `direction` 变化时：
   - 如果是 `INBOUND`，会强制 payment method 变成 `VIRTUAL_ACCOUNT`
   - 如果是 `OUTBOUND`，保留 beneficiary 逻辑
   - 同时重新加载推荐路由
2. 当 `merchantId` 变化时：
   - customer 列表会按商户过滤
   - beneficiary 列表会按商户过滤
   - source account 列表会按商户过滤
   - 如果有匹配项，会自动带出第一条

#### 创建支付订单流程

点击 `Create Payment Order` 时：

1. 前端根据表单拼出 payload
2. 把 `amount` 转成数值
3. 通道选择有两种：
   - 用户手工指定某个通道
   - 留空，走自动路由
4. 前端调用：
   - `POST /api/payment/orders`
5. 后端生成支付订单，并做幂等检查
6. 前端刷新订单列表
7. 右侧展示最近一次响应

#### 审批流程

每个订单卡片会根据当前状态展示不同按钮。

可用动作：

- Checker approve / reject
- L1 approve / reject
- L2 approve / reject

按钮出现条件：

- checker 只在 `PENDING_CHECKER_REVIEW`
- L1 只在 `PENDING_L1_REVIEW`
- L2 只在 `PENDING_L2_REVIEW`

其中 L2 approve 是关键动作：

1. 后端开始提交到 gateway
2. 如果用户指定了通道，就走指定通道
3. 如果没指定，就走自动路由推荐
4. gateway 适配器负责做字段转换和调用
5. 订单状态进入：
   - `GATEWAY_SUBMITTED`
   - 或 `FAILED`

#### 运营动作

提交之后，页面还支持：

- cancel
- mark completed
- mark failed
- retry submit

这样当前 MVP 已经具备了从订单创建、审批、通道提交到运营处理的一整条最小闭环。

#### 查询筛选

查询区调用：

- `GET /api/payment/orders`

支持的过滤条件：

- merchantId
- status
- direction
- channelCode
- keyword

这是目前 payment 模块的运营列表页。

## 5. 当前后端请求流转

### 5.1 Gateway 链路

一个 gateway 请求的处理过程是：

1. controller 接收统一请求
2. `ChannelGatewayService` 调用 registry
3. `ChannelRegistry` 根据 `channelCode` 找到适配器
4. 适配器把统一字段转换成下游字段
5. 适配器执行：
   - mock 通道逻辑
   - 或真实 SGB 逻辑
   - 或 SGB 模拟模式
6. `ChannelGatewayService` 记录：
   - 监控执行结果
   - 网关审计日志
7. 最终把响应返回给前端

### 5.2 Payment 链路

一个 payment 订单的处理过程是：

1. 前端发起创建请求
2. `PaymentService` 先检查幂等
3. 订单以 `PENDING_CHECKER_REVIEW` 存库
4. 审批按顺序推进：
   - Checker
   - L1
   - L2
5. 到 L2 approve 时：
   - 入金场景可能创建 virtual account
   - 出金场景则调用 payout
6. payment service 调用 gateway service
7. 同时记录监控和网关审计
8. 回写订单的 routed channel、gateway request id、gateway message、status

### 5.3 Routing 与 Monitoring 链路

当请求路由推荐时：

1. `ChannelRoutingService` 读取当前通道目录
2. 向监控服务拿每个通道在该操作下的快照
3. 按成功率和延迟排序
4. 返回推荐列表
5. 同时把推荐结果写入 `ops_db`

当 gateway 真正执行时：

1. `ChannelGatewayService` 记录成功失败和耗时
2. `ChannelMonitoringService` 更新内存中的累计值
3. 同时把快照落库到 `ops_db.channel_metric_snapshot`

## 6. 当前通道接入情况

### 6.1 Mock 通道

- `APEX_PAY`
- `HARBOR_SWITCH`

作用：

- 验证统一网关协议是否合理
- 验证字段转换架构是否成立
- 在真实通道未完全接好前，保证前端和支付流程可以持续推进

### 6.2 SGB

当前 SGB 已完成：

- virtual account 映射
- remittance payout 映射
- intra-bank payout 映射
- webhook 标准化
- 签名逻辑骨架
- 未配置密钥时的模拟成功模式

当前限制：

- 真正生产流量还需要真实 host 和真实 credentials

## 7. 目前已经具备的企业级基础

当前项目已经具备一些比较像企业级系统的基础能力：

- 后端模块化结构
- 公共 common 模块
- 统一网关协议
- 通道适配器模式
- 审批流
- 明确的状态机
- 幂等保护
- 基于指标的路由
- 监控持久化
- 审计持久化
- 按业务域拆分数据库

## 8. 当前还缺什么

如果要进一步往企业级上线版本推进，当前还缺的关键能力主要有：

- 认证与授权
- maker-checker 权限边界
- 商户级通道凭证管理
- 商户级路由策略管理
- 风控与合规校验
- webhook 验签和防重放
- 重试与对账策略
- 通知与告警
- 配置中心和密钥管理
- 标准化错误码体系
- 更完整的可观测性面板和告警阈值

## 9. 近期推荐演进方向

比较顺的下一步是：

1. 继续把 `payment` 模块补全
2. 把 payment 模块内部再细分成：
   - api
   - application
   - domain
   - infrastructure
3. 增加认证和角色权限
4. 增加商户和通道凭证管理
5. 做 SGB 真实 smoke test
6. 后续再决定是否从一个 Boot app 拆成多个独立服务

## 10. 后续如何继续协作

新开一个对话时，先发：

```text
Please read docs/codex-project-context.md, docs/payment-platform-blueprint.md, docs/channel-gateway-mvp.md, docs/project-overview.md, docs/project-overview.zh-CN.md, and docs/current-status.md before continuing.
```

这样是目前最稳妥的续接方式。
