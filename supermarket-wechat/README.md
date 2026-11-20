# 邻里小超 · 微信小程序

微信原生小程序 + TypeScript，与 H5 / 管理后台共用同一套 Spring Boot API。

## 导入与运行

1. 打开微信开发者工具 →「导入项目」→ 目录选择 `D:/wordbuudy/2026-11-20-08-52-55/supermarket-wechat`。
2. AppID 目前是占位值 `touristappid`（见 `project.config.json`）。正式调试请替换为自己的测试号 AppID。
3. 在「详情 → 本地设置」勾选 **不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书**，否则模拟器无法访问 `http://localhost:8080`。
4. 先启动后端（`mvn spring-boot:run` 或 H2 profile），再编译小程序即可。

### 真机调试

模拟器用 `localhost` 没问题；真机需把 **`config.ts`** 中的地址改成电脑的局域网 IP：

```ts
export const BASE_URL = 'http://192.168.1.20:8080/api'
```

手机与电脑连同一 Wi-Fi，并确保电脑防火墙放行 8080 端口。

### 类型检查（可选）

```bash
npm install
npm run typecheck
```

## 演示账号

- 手机号 `13912348765`，验证码固定 `123456`（登录页有一键填入）
- 任意新手机号 + `123456` 首次登录自动注册（建默认地址 + 发新人券）

## 页面与组件

`app.json` 注册 10 个页面：index / category / list / detail / cart / checkout / orders / order-detail / mine / login。
其中 index、category、cart、orders、mine 使用 `components/tab-bar` 自定义底部导航。
`components/product-card` 展示商品图、名称、价格与库存紧张标记。

主旅程：首页浏览 → 分类/列表 → 详情加购 → 购物车勾选 → 结算选地址/券并实时试算 → 提交订单 → 微信/支付宝模拟支付 → 订单详情确认收货。

## 统一口径

- 后端直连 `http://localhost:8080/api`，响应 `{code,data,message}`，`code=0` 成功。
- Token 存 `wx.setStorageSync('linli_token')`，请求头 `Authorization: Bearer <token>`。
- 金额展示 `¥xx.xx`；订单状态 0待付 / 1待配送 / 2配送中 / 3完成 / 4取消。
- `/coupons/available?amount=商品金额` 返回的 `userCouponId` 即结算提交时的 `couponId`。
