# 项目名称

## 简介
**dhlyf-wallet** 是一个专为中心化钱包充提业务设计的企业级解决方案，主要功能包括：分配充值地址、充值到账、资金归集及提现操作。系统分为**冷热钱包**架构：
- **热钱包**：负责扫链、检测交易、充值入账和提现出金等实时操作。
- **冷钱包**：负责创建地址（密钥加密）和离线交易签名，确保安全性，建议完全断网运行以最大限度降低风险。

### 支持的主链
系统已支持主流公链，包括：  
**ALEO, BSC, BTC, ETH, FIL, TRON, SUI, SOL**。

### 性能指标
单链单程序在 **2核4G** 的环境下，1分钟可处理 **5万笔交易**，主要性能瓶颈在于RPC服务的网络消耗。

---

## 功能特性
- **账户管理**
    - 热钱包账户：主要用于出金，按需存储少量资金，每日根据提现数量进行一次补充。
    - 用户充值账户：监控充值地址的资金流动，实时显示入账和归集状态。

- **资产查看**
    - 实时查看热钱包账户和用户充值账户的余额及各类数字资产明细。

- **充值**
    - 自动分配用户充值地址，监控链上交易并快速入账，保障用户体验。

- **提现**
    - 支持批量提现，链上透明可查，实时推送交易状态。

- **归集**
    - 自动归集用户充值资金至冷钱包，优化资金管理，减少热钱包风险。

- **安全性**
    - 服务通信数据加密，确保无法被破解。
    - 数据指纹签名，防止数据被篡改。
    - 冷钱包隔离网络环境，确保密钥与签名安全。

---

## 技术架构
- **前端**：使用 `React` 或 `Vue.js` 框架，支持桌面端与移动端自适应。
- **后端**：核心业务采用 **JAVA** 和 **RUST** 实现，高性能与安全并存。
- **中间件**：集成 **Redis**（缓存）、**MySQL**（数据存储）和 **Kafka**（消息队列）处理高并发数据流。
- **区块链交互**：依赖私有节点提供的RPC服务，保障交易稳定性和响应速度。
  > ⚠️ 不建议使用公共节点，避免因流量限制导致的交易积压和处理失败。

---

## 文件结构
