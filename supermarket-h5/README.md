# 邻里小超 · H5 移动端

Vue 3 + TypeScript + Vite 7 + Pinia + Vue Router 4，端口 `5173`。

## 启动

```bash
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。`/api` 请求由 Vite 代理到 `http://localhost:8080`，请先启动 Spring Boot 后端。

```bash
npm run typecheck   # 仅类型检查
npm run build       # 类型检查 + 生产构建
```

## 演示账号

- 用户：`13912348765` / 验证码 `123456`（登录页有「使用演示账号」快捷按钮）
- 任意新手机号 + `123456` 首次登录自动注册并自动发新人券
- 管理后台账号在 `supermarket-web-manager`，端口 `5174`

## 目录说明

```
src/
├── api/        10 个接口模块 + request.ts（统一拆包 Result、注入 Bearer Token、401 跳登录）
├── components/ TabBar / ProductCard / SkuBar / AmountPanel / AddressCard / CouponPicker / EmptyState
├── router/     14 个路由，meta.requiresAuth 守卫
├── store/      user（token/profile/login/logout）、cart（与后端双向同步）、app（门店信息/loading）
├── types/      后端 VO/DTO 的 TypeScript 镜像 + 状态枚举文案
├── utils/      auth（linli_token 持久化）、format（¥两位小数/日期/Toast）
├── views/      14 个页面
└── styles/     CSS 变量 + 全局样式
```

## 金额与状态口径

- 金额一律 `¥xx.xx` 两位小数，试算由后端 `/orders/preview` 返回，前端只做展示。
- 订单状态：0待付款 / 1待配送 / 2配送中 / 3已完成 / 4已取消。
- 券状态：0未使用 / 1已使用 / 2已过期 / 3锁定中。
- 结算时 `/coupons/available?amount=商品金额` 返回的 `userCouponId` 作为 `couponId` 提交。
- Token 存储 key 为 `linli_token`，与管理后台一致（两端互不干扰，分别在不同端口的 localStorage 域下）。
