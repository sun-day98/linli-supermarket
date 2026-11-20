# 邻里小超 · Web 管理后台

Vue 3 + TypeScript + Vite 7 + Element Plus + ECharts + Pinia，端口 `5174`。

## 启动

```bash
npm install
npm run dev
```

浏览器访问 `http://localhost:5174`。`/api` 请求由 Vite 代理到 `http://localhost:8080`，请先启动 Spring Boot 后端。

```bash
npm run typecheck
npm run build
```

## 演示账号

管理员：`admin` / `admin123`。登录页已预填。

> Token key 与 H5 相同（`linli_token`），但两端运行在不同端口，浏览器 localStorage 相互隔离。

## 功能模块

| 路由 | 页面 | 说明 |
|---|---|---|
| `/dashboard` | 数据概览 | 8 项指标卡 + 近 N 日销售趋势（ECharts）+ 最新订单 |
| `/orders` | 订单管理 | 筛选/分页/详情抽屉，模拟收款、发货、完成、取消 |
| `/products` | 商品管理 | 含库存紧张筛选，新增/编辑弹窗，上下架开关 |
| `/categories` | 分类管理 | 行内改排序并同步前端 |
| `/reviews` | 评价管理 | 通过/拒绝/删除 |
| `/members` | 会员管理 | 由 `/admin/orders` 聚合的消费概览（见下方说明） |
| `/coupons` | 优惠券管理 | 券 CRUD、类型/门槛/有效期、新人自动发放开关 |
| `/delivery` | 配送管理 | 城市/区县树维护 + 配送员管理 |
| `/store` | 门店设置 | 名称/宣传语/配送费/免配送门槛/客服电话 |

## 已知口径说明

演示后端 62 个端点中未提供独立的「会员列表」接口，因此「会员管理」页面通过分页拉取 `/admin/orders` 并按 `userId` 聚合出订单数、累计消费与最近下单时间。若后续后端补充 `/admin/members`，只需替换 `src/api/member.ts` 中 `getMembers()` 的实现，页面无需改动。

金额统一展示为 `¥xx.xx`；订单状态 0待付 / 1待配送 / 2配送中 / 3完成 / 4取消。
