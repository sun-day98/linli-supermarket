# 邻里小超社区电商系统 · Spring Boot 后端

Java 17 + Spring Boot 3.4 + MyBatis-Plus 3.5 + MySQL 8（可选 H2 内存库），端口 `8080`，API 前缀 `/api`。

## 一、启动方式

### 方式 A：MySQL（推荐）

1. 安装并启动 MySQL 8，确认账号密码（默认配置 `root` / `root`）。
2. 建库（连接 URL 已带 `createDatabaseIfNotExist=true`，也可手动执行）：

```sql
CREATE DATABASE IF NOT EXISTS supermarket_h5
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

1. 若本机 MySQL 账号密码不是 `root/root`，修改 `src/main/resources/application.yml` 中的  
   `spring.datasource.username` 与 `spring.datasource.password`。
2. 启动：

```bash
cd springboot-supermarket
mvn spring-boot:run
```

`schema.sql` 会在启动时自动建 15 张表（`CREATE TABLE IF NOT EXISTS`），`DataInitService` 在空库时自动播种演示数据。重复启动不会重复写入。

### 方式 B：H2 内存库（无 MySQL 环境）

```bash
cd springboot-supermarket
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

或打包后运行：

```bash
mvn clean package -DskipTests
java -jar target/springboot-supermarket-1.0.0.jar --spring.profiles.active=h2
```

H2<u> 数据仅存在于内存中，每次启动都是空库并重新播种。H2 Console：`http://localhost:8080/h2-console`（JDBC URL 见 `application.yml` 的 h2 profile）。</u>

## 二、演示账号

| 端        | 账号                      | 密码 / 验证码   | 说明                            |
| -------- | ----------------------- | ---------- | ----------------------------- |
| H5 / 小程序 | `13912348765`           | `123456`   | 演示会员，含地址、购物车、优惠券、历史订单与 128 积分 |
| H5 / 小程序 | 任意新手机号（如 `13800000000`） | `123456`   | 首次登录自动注册：建用户 + 默认地址 + 新人券     |
| 管理后台     | `admin`                 | `admin123` | 管理员，BCrypt 加密存储               |

验证码固定 `123456`，发券与支付均为模拟，不产生真实扣款。

## 三、全局约定

1. 响应体统一 `{ "code": 0, "data": ..., "message": "ok" }`，`code=0` 成功。
2. 鉴权头 `Authorization: Bearer <token>`；前端存储 key 为 `linli_token`。
3. Token 为自研 HMAC-SHA256 无状态令牌（userId + role + exp），有效期 7 天，密钥见 `app.token.secret`。
4. 金额一律 `BigDecimal`，输出两位小数（`HALF_UP`），禁止 float/double 算钱。
5. 日期时间格式 `yyyy-MM-dd HH:mm:ss`。
6. 状态口径：订单 `0待付/1待配送/2配送中/3完成/4取消`；用户券 `0未用/1已用/2过期/3锁定`；商品 `0下架/1上架`；积分流水 `1获取/2使用/3回滚`；评价 `0待审/1通过/2拒绝`。
7. 分页：请求 `current`（从 1 起）/ `size`，响应 `{records,total,current,size}`。
8. 错误码：`400` 参数错误、`401` 未登录、`4001` 库存不足、`4002` 券不可用、`4003` 订单状态非法。
9. DB 下划线 ↔ Java 驼峰，由 MyBatis-Plus `map-underscore-to-camel-case` 自动转换。

## 四、核心接口速览

| 方法   | 路径                                       | 说明                                |
| ---- | ---------------------------------------- | --------------------------------- |
| POST | `/api/auth/login`                        | 用户手机号+验证码登录（新用户自动注册）              |
| POST | `/api/auth/admin-login`                  | 管理员账密登录                           |
| GET  | `/api/auth/me`                           | 当前登录信息（需 Token）                   |
| GET  | `/api/products` / `/api/products/{id}`   | 商品列表 / 详情（公开）                     |
| GET  | `/api/categories`                        | 启用分类（公开）                          |
| GET  | `/api/cart` · POST `/api/cart`           | 购物车                               |
| GET  | `/api/addresses` · POST `/api/addresses` | 地址                                |
| POST | `/api/orders/preview`                    | 金额试算 `{items,addressId,couponId}` |
| POST | `/api/orders`                            | 提交订单（扣库存 / 锁券）                    |
| POST | `/api/orders/{id}/pay`                   | 模拟支付 `{payType}`（CAS 幂等）          |
| POST | `/api/orders/{id}/finish`                | 确认收货（发积分）                         |
| POST | `/api/orders/{id}/cancel`                | 取消订单（回滚库存/券/积分）                   |
| GET  | `/api/coupons/available?amount=xx`       | 可用券（按门槛过滤）                        |
| GET  | `/api/points/records`                    | 积分明细                              |
| GET  | `/api/delivery/areas`                    | 启用配送区域树（公开）                       |
| GET  | `/api/store`                             | 门店设置（公开）                          |
| GET  | `/api/admin/stats/overview`              | 经营概览                              |
| GET  | `/api/admin/stats/trend?days=7`          | 销售趋势                              |

完整接口清单见 `docs/ARCH-邻里小超社区电商系统.md` 第 3.2 节。

## 五、交易一致性说明

- **防超卖**：`ProductMapper.deductStock` 使用 `UPDATE product SET stock=stock-n WHERE id=? AND status=1 AND stock>=n`，按影响行数判定，失败抛 `BusinessException(4001)`。
- **金额口径**：`preview` 与 `createOrder` 共用私有方法 `calcAmount()`；提交时后端重新读取商品单价与门店配送费，忽略前端价格；可用 `expectedPayAmount` 做展示与提交一致性校验。
- **锁券**：下单 `validateAndLock` 将 `user_coupon.status` 由 `0→3`；支付 `consume` `3→1`；取消 `release` `1/3→0`。
- **支付幂等**：`pay()` 以 `UPDATE orders SET status=1 WHERE id=? AND status=0` CAS 推进，重复调用直接返回当前订单。
- **积分**：确认收货按实付金额向下取整发放（1 元 = 1 积分），取消已完成订单回滚，均有流水且幂等。
- **事务边界**：下单 / 支付 / 发货 / 收货 / 取消均加 `@Transactional(rollbackFor = Exception.class)`，异常统一抛 `BusinessException` 触发回滚。

## 六、常见问题

- **端口被占用**：修改 `application.yml` 的 `server.port`，同时同步前端 Vite proxy 目标。
- **跨域**：`WebConfig` 已放行全部来源（含 5173/5174 与小程序），开发调试无需额外配置。
- **数据想重置**：MySQL 下执行 `DROP DATABASE supermarket_h5;` 后重新建库再启动；H2 下重启进程即可。
- **白名单**：仅 `/api/auth/login`、`/api/auth/admin-login` 与公开 GET（`/api/products/**`、`/api/categories`、`/api/store`、`/api/delivery/areas`、`/api/reviews`）免 Token；`GET /api/categories/all` 已收权需 Token；管理端 `/api/admin/**` 需管理员 Token。

## 七、质量门禁

项目已纳入 Git，并将 `mvn test` 固化为提交门禁：

- **CI（自动）**：`.github/workflows/ci.yml` 在 push / PR 到 `main`、`master` 时自动执行后端 94 用例 + 前端三端构建（H5 / 后台 `npm run build`，小程序 `npm run typecheck`）。
- **本地（提交前）**：`bash scripts/verify.sh`（Git Bash / Linux / macOS）或 `scripts/verify.bat`（Windows CMD）。默认执行后端 94 用例 + 三端类型检查（无文件删除，适配本机沙箱策略）；需要完整打包时设置 `FULL_BUILD=1`。
- **测试稳定性**：`src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` 固定为 `mock-maker-subclass`，避免 Mockito 5 默认 inline mock maker 在部分环境自附加 agent 失败（曾导致 84 个用例报错）。

## 八、配置外部化（安全基线）

以下配置均支持环境变量覆盖，演示默认值保证开箱即用：

| 环境变量 | 用途 | 默认值 |
| --- | --- | --- |
| `APP_TOKEN_SECRET` | Token 签名密钥（未设置时启动输出安全告警） | 演示密钥 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号密码 | `root` / `root` |
| `APP_CORS_ORIGINS` | CORS 白名单，逗号分隔；`*` 时自动禁用凭证携带 | `*` |
| `DEMO_MODE` / `DEMO_VERIFY_CODE` | 演示验证码开关与验证码值 | `true` / `123456` |

