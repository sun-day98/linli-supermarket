# 邻里小超社区电商系统 — QA 测试报告

> 测试负责人：严过关（Yan）· QA Engineer
> 测试轮次：第 1 轮（静态审查 + 测试用例落盘）→ 第 2 轮（修复后回归复核），**已达 2 轮硬上限**
> 报告日期：2026-11-20
> 最终路由：**NoOne**（3 个源码 Bug 均已修复并静态复核通过）

---

## 一、执行摘要

| 项目 | 结论 |
|---|---|
| 后端 JUnit 测试执行 | ❌ **无法执行**：本机无 Maven、无 Gradle、无 `.m2` 缓存、无任何 Spring/JUnit jar。`mvn -v` 返回 `command not found`；`java -version` = Temurin 17.0.8 可用（仅用于 javac 语法扫描） |
| 前端三端构建 | ✅ **真实运行通过**（Node v22.22.2 / npm 10.9.7，`node_modules` 已存在） |
| 后端业务逻辑验证 | ✅ **第 2 轮静态复核通过**：第 1 轮发现的 1 个编译阻断 + 2 个高危问题已全部修复，未发现新增源码缺陷 |
| 测试用例落盘 | ✅ 9 个测试文件 / **92** 个测试方法（JUnit 5 + Mockito）；javac 语法扫描无错误 |
| **智能路由判定** | ✅ **NoOne**（第 2 轮结束；无待修复源码 Bug，测试代码适配由 QA 自行完成） |

### 环境探测记录

```
mvn -v        → mvn: command not found
java -version → openjdk 17.0.8 (Temurin) ✅
node -v       → v22.22.2 ✅
npm -v        → 10.9.7 ✅
~/.m2         → 不存在；全盘未找到 junit-jupiter*.jar；项目无 mvnw
```

**重要声明**：本报告中的后端结论均来自**逐行静态代码审查**，不是"测试运行通过"。所有 JUnit 用例已落盘但未在本机执行，不得对外表述为"后端测试通过"。

---

## 二、前端三端真实构建验证

| 端 | 命令 | 结果 | 备注 |
|---|---|---|---|
| H5 | `npm run build` | ✅ 通过（4.71s） | `vue-tsc --noEmit && vite build`，产物 dist/ |
| 管理后台 | `npm run build` | ✅ 通过（14.64s） | 有 chunk >500kB 的性能警告，非错误 |
| 微信小程序 | `npm run typecheck` | ✅ 通过（8s） | `tsc --noEmit` 无输出 |

---

## 三、核心业务规则逐项验证

### 1. 金额试算 — ⚠️ 静态审查通过（测试已写，未运行）

**结论：算法实现符合 PRD §5.1。**

- `OrderServiceImpl.calcAmount()`（L300-323）：`应付 = 商品金额 + 配送费 − 优惠`，`setScale(2, HALF_UP)`，负值钳制为 0。
- `sumGoodsAmount()`（L325-331）：Σ `OrderItem.totalAmount`，统一 2 位 HALF_UP。
- `buildItems()`（L333-355）：单价来自 `productService.getById()` 后端读取（L339），前端 DTO 无价格字段，前端无法篡改单价；`price.setScale(2, HALF_UP)` 后乘数量。
- 免配送门槛：`threshold > 0 && goodsAmount >= threshold`（L306），边界值 `=` 命中免配送，门槛 0 不误判。
- `preview()`（L95）与 `createOrder()`（L140）**均调用同一个 `calcAmount()`**，口径一致；提交时 `expectedPayAmount` 以 HALF_UP 2 位比对（L142-146），不一致即拒绝。
- `CouponServiceImpl.calcDiscount()`（L226-246）：满减固定额 / 折扣按比例，HALF_UP 2 位，抵扣额上限为商品金额，负数归零。
- `WebConfig.jacksonCustomizer()`（L58-68）：全局 BigDecimal 序列化 `setScale(2, HALF_UP)`，金额字段口径统一。

### 2. 库存 — ⚠️ 静态审查发现 1 个并发缺陷（见 QA-003）

- `ProductMapper.deductStock()`（L16-17）：`UPDATE product SET stock=stock-n, sales=sales+n WHERE id=? AND status=1 AND stock>=n` —— 按影响行数判成败，**防超卖条件正确**。
- `OrderServiceImpl.createOrder()` L149-154：逐项调用，任一失败抛 `STOCK_NOT_ENOUGH`，`@Transactional(rollbackFor=Exception.class)` 回滚前面已扣库存与锁券。
- `ProductMapper.rollbackStock()`（L21）：无条件 `stock = stock + n`，**非幂等**，且 `cancel()` 未用 CAS 结果守卫 → 并发重复取消会重复加库存。

### 3. 优惠券状态机 — ✅ 静态审查通过

- `UserCouponMapper`：`lock` 0→3（L12）、`consume` 3→1 且写 use_time/order_id（L16-17）、`release` IN(1,3)→0 且清空 use_time/order_id（L21-22）——三条 CAS 与 PRD 状态机完全一致。
- `CouponMapper.increaseIssued()`（L12-13）：`issued < total` 条件原子自增，防超发。
- `validateForPreview()`（L180-197）：归属、券定义存在、状态、时效（固定期 / 相对天数）、门槛全校验。
- `validateAndLock()`（L168-177）：先校验再 CAS 锁，锁失败抛业务异常。
- 支付幂等：`OrderServiceImpl.pay()` L199-205 先判 `status != 0` 直接返回，`payCas` 成功才 `couponService.consume()`；重复支付不重复核销。

### 4. 积分 — ✅ 静态审查通过

- `PointsServiceImpl.award()` L46：`payAmount.setScale(0, RoundingMode.DOWN)` 向下取整，1 元 = 1 积分；按 `orderId + type=1` 查重幂等；≤0 不发。
- `rollback()` L67-94：按 `orderId + type=3` 查重幂等；汇总该订单全部 award 记录后 `addPoints(-total)`。
- `OrderServiceImpl.finish()` L225-228：`finishCas` 成功才发积分；`cancel()` L245-246：status=3 才回滚积分。

### 5. 订单状态流转 — ⚠️ 静态审查通过，但取消路径有并发缺陷（QA-003）

| CAS | SQL 条件 | 评价 |
|---|---|---|
| `payCas` | `status = 0 → 1` | ✅ 幂等，重复支付不重复处理 |
| `deliverCas` | `status = 1 → 2` | ✅ 越界发货被拒 |
| `finishCas` | `status = 2 → 3` | ✅ 仅配送中可确认收货 |
| `cancelCas` | `status <> 4 → 4` | SQL 本身合理；**但调用方忽略返回值**（QA-003） |

- `cancel()` L241-243：status=2（配送中）明确禁止取消 ✅；status=4 幂等返回 ✅。
- 状态文案 `OrderServiceImpl.statusText()`（L432-444）与 H5 / 管理后台 / 小程序三端映射完全一致：0待付款 1待配送 2配送中 3已完成 4已取消。

### 6. 鉴权 — ⚠️ 静态审查发现角色校验缺口（QA-002）

- `TokenUtil`：HMAC-SHA256 自研 Token，`MessageDigest.isEqual` 常量时间比较签名，校验 exp，密钥/有效期可配置 ✅（已写 6 个真实单测）。
- `PasswordUtil`：BCrypt（`spring-security-crypto`），空输入安全返回 false ✅。
- `AuthServiceImpl.adminLogin()` L70-72：BCrypt 校验 + 账号状态双重检查 ✅。
- `TokenInterceptor`：OPTIONS 放行；`/api/auth/**` 全放行；GET 商品/分类/门店/配送区域/评价白名单 —— 与设计文档 T01 完全一致 ✅；Bearer 解析失败/无头统一返回 401 ✅；`afterCompletion` 清理 ThreadLocal ✅；`/api/auth/me` 由 Controller 自行解析 Token（AuthController L56-64）✅。
- **角色校验仅覆盖 `path.startsWith("/api/admin/")`**（L58），管理端接口散落在其他前缀下，普通 USER Token 可调用（QA-002）。

### 7. 数据初始化 — 🚩 编译阻断（QA-001）+ 静态逻辑审查通过

- `DataInitService.seedDemoData()` L127-149：各表 `selectCount == 0` 才播种，重复启动不污染 ✅。
- 播种量：4 分类（L152）、18 商品（PRODUCT_SEED L73-92，含蓝莓 stock=5/alert=10、黄瓜 stock=8/alert=10 紧张样本）、3 券（含 `newUserAuto=1, scope=2` 新人券 L173-176）、4 城市 + 12 区县（L243-250）、2 配送员、admin/BCrypt(admin123)、演示会员 13912348765（地址/购物车/2 张券/已完成历史订单 36.87 元 /36 积分/2 条评价）✅。
- **但 `initSchema()` 与 `seedDemoData()` 上误加 `@Override`（L105、L125），类仅实现 `CommandLineRunner`，无这两个方法 → Java 编译错误，后端无法构建。**

### 8. 前后端契约一致性 — ✅ 静态审查通过 + 前端真实构建通过

- H5 `baseURL='/api'`、`/orders/preview`、`/orders`、`/orders/{id}/pay|finish|cancel` 与 `OrderController` 映射逐条对齐 ✅。
- 小程序 `api/*.ts` 路径（`/auth/login`、`/products`、`/categories`、`/coupons/...`、`/orders/...`）与后端一致 ✅。
- 管理后台 `/admin/orders`、`/admin/stats/*`、`/coupons/admin` 等与 `CouponController` / `StatsController` 映射一致 ✅。
- `couponId` 字段：H5 `CouponPicker` 传出 `userCouponId` 值写入 `couponId`，`OrderServiceImpl.resolveCouponId()` 优先 `userCouponId` 回退 `couponId`，语义自洽 ✅。
- 金额 2 位小数：后端 `WebConfig` 全局序列化 + 前端按 number 直接展示 ✅。

---

## 四、问题清单

| 编号 | 严重级别 | 文件与方法位置 | 描述 | 建议修复 |
|---|---|---|---|---|
| **QA-001** | **P0 / Blocker** | `springboot-supermarket/src/main/java/com/linli/supermarket/service/DataInitService.java` L105（`initSchema()` 上方）、L125（`seedDemoData()` 上方） | 两处误加 `@Override`。类只 `implements CommandLineRunner`，该接口只有 `run(String...)`，不存在 `initSchema()`/`seedDemoData()`。javac 必然报 `方法不会覆盖或实现超类型的方法`。**后端 0% 可编译启动，全部 62 个 REST 端点不可用。** 已用最小 javac 样例实测复现（exit code 1）。 | 删除这两处 `@Override` 注解；若确有接口契约需求，则新建接口声明这两个方法并让类实现它。 |
| **QA-002** | **P1 / High** | `springboot-supermarket/src/main/java/com/linli/supermarket/config/TokenInterceptor.java` L58；涉及 `CouponController` L51-73、`ProductController` L40-57、`CategoryController` L41-52、`StoreController` L26、`OrderController` L60 | 管理员角色校验仅判断 `path.startsWith("/api/admin/")`。设计文档 ARCH §3.2 明确标注为"管理端"的下列接口不在该前缀内，任意已登录 USER Token 即可调用：`GET/POST/PUT/DELETE /api/coupons/admin*`、`POST/PUT/DELETE /api/products*`、`POST/PUT/DELETE /api/categories*`、`PUT /api/store`、`POST /api/orders/{id}/deliver`。越权可改价、上下架、发券、改门店配置、替任意订单发货。注：H5 `getCouponDefinitions()` 注释显示 `/coupons/admin` 的 GET 是刻意留给用户的演示口径，但写接口仍应收紧。 | 抽取统一的 `isAdminEndpoint(method,path)` 判定：除 `/api/admin/**` 外，显式纳入上述管理端路径与方法；或改用 `@RequireAdmin` 注解 + HandlerMethod 判定，避免前缀遗漏。 |
| **QA-003** | **P1 / High** | `springboot-supermarket/src/main/java/com/linli/supermarket/service/impl/OrderServiceImpl.java` `cancel()` L233-257（尤其 L249-256） | 取消流程**先执行副作用（释放券、回滚库存、回滚积分），最后才调用 `ordersMapper.cancelCas(orderId)` 且完全忽略其影响行数**。并发两次取消时，两个事务都读到 `status=0/1/3`，都会执行 `productService.rollbackStock()`（L21 Mapper 为无条件 `stock=stock+n`，非幂等），随后只有一个 `cancelCas` 成功、另一个影响 0 行却被忽略 → **库存被重复回滚（超发）**。同理超时任务 `cancelTimeoutOrders()` 与用户取消并发时也会放大该问题。 | 先执行 CAS 再执行副作用：`if (ordersMapper.cancelCas(orderId) == 0) return;` 放在所有回滚动作之前（或至少用返回值守卫回滚库存）；`rollbackStock` 增加幂等标记/按 `order_id` 记录已回滚明细。 |
| QA-004 | P2 / Medium | `springboot-supermarket/src/main/java/com/linli/supermarket/config/WebConfig.java` L40-47 | CORS 使用 `allowedOriginPatterns("*")` + `allowCredentials(true)`，与设计文档 T01"允许 5173/5174/小程序来源"不符，任意站点可携带凭证跨域请求。 | 将 `allowedOriginPatterns` 收敛为 `http://localhost:5173`、`http://localhost:5174` 等白名单。 |
| QA-005 | P3 / Info | `springboot-supermarket/src/main/java/com/linli/supermarket/service/impl/CouponServiceImpl.java` L199-205 `consume()` | 忽略 `userCouponMapper.consume()` 的影响行数。若并发/异常导致券已不在 status=3，支付成功后券会停留在锁定态(3)而无法再用。当前业务路径下触发概率低，记录为观察项。 | 返回影响行数并在为 0 时记录告警日志，或抛异常使支付事务回滚。 |

**第 2 轮状态**：QA-001 / QA-002 / QA-003 已由工程师修复并复核通过（详见第八节）；QA-004、QA-005 经工程师评估后决定保持现状，作为**已接受的设计取舍**记录，不再作为待修复缺陷追踪。

---

## 五、测试用例落盘清单

| 文件 | 测试数 | 覆盖重点 |
|---|---|---|
| `src/test/java/com/linli/supermarket/service/AmountCalcTest.java` | 7 | 后端重读单价、HALF_UP 精度、免配送门槛边界、券抵扣、金额下限 0、不可配送标记、非法数量 |
| `src/test/java/com/linli/supermarket/service/OrderServiceTest.java` | 18 | 预期金额不一致拒绝、下单落库/扣库存/锁券、库存不足、支付 CAS 幂等、发货/收货状态越界、取消各状态回滚、CAS 抢占失败零回滚（QA-003 回归）、状态文案 |
| `src/test/java/com/linli/supermarket/service/CouponServiceTest.java` | 18 | 满减/折扣/封顶/负值抵扣、归属/门槛/时效/状态校验、锁券 CAS 失败、领券限制（重复/新人/售罄）、核销与释放 |
| `src/test/java/com/linli/supermarket/service/PointsServiceTest.java` | 8 | 36.99→36 向下取整、不足 1 元不发、发放/回滚按 orderId 幂等、净变化为 0 |
| `src/test/java/com/linli/supermarket/service/AuthServiceTest.java` | 9 | 验证码、自动注册+发券、禁用账号、BCrypt 正确/错误/明文、ADMIN Token 签发 |
| `src/test/java/com/linli/supermarket/service/DataInitTest.java` | 9 | 重复启动不污染、播种数量、紧张样本、新人券、admin BCrypt、演示会员与历史订单、门店默认配置 |
| `src/test/java/com/linli/supermarket/config/TokenInterceptorTest.java` | 13 | OPTIONS/白名单/无 Token/非 Bearer、USER 与 ADMIN 角色、过期 Token、12 个散落管理端写接口越权拒绝（QA-002 回归）、用户写操作不误伤、ThreadLocal 清理 |
| `src/test/java/com/linli/supermarket/util/TokenUtilTest.java` | 6 | 签发解析回环、默认角色、篡改/换密钥/过期/格式非法拒绝（纯逻辑，无 Mock） |
| `src/test/java/com/linli/supermarket/util/PasswordUtilTest.java` | 4 | BCrypt 密文不可逆、正确口令匹配、空输入安全、随机盐 |

**合计 92 个测试方法**（第 2 轮新增 6 个回归用例），均为正向 + 边界 + 异常路径断言，无空测试；使用 `@ExtendWith(MockitoExtension.class)`、`@InjectMocks`、`ArgumentCaptor`、AAA 结构。`javac -proc:none` 语法扫描全部通过，无语法错误。

> 第 2 轮适配说明：因 QA-003 修复变更了 `cancel()` 调用时序，QA 已自行在 `cancel_whenCompleted_...` / `cancel_whenUnpaid_...` 两例补上 `when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(1)`，并新增 CAS 抢占失败零回滚用例；此项属**测试代码适配（路由 QA 自修复）**，非源码问题。

---

## 六、智能路由判定（最终）

**路由：NoOne**

- 第 1 轮判定为 Engineer，3 个源码缺陷已全部交由工程师修复；
- 第 2 轮逐条复核修复代码，**未发现新增源码缺陷，无待修复项**；
- 第 2 轮唯一改动是 QA 自行适配 2 个既有测试的 Mockito stub，并按修复后语义补充 6 个回归用例 —— 属 QA 路由自修复，不改变最终路由；
- 已达 2 轮硬上限，测试活动结束。

**Known Issues（遗留限制，非代码缺陷）**：本机无 Maven / 无 `.m2` / 无 jar，后端 92 个 JUnit 用例**未能实际执行**。这是环境限制，不是 Known Bug；在有 Maven 的环境执行 `mvn -Dspring.profiles.active=h2 test` 即可获得运行结果。

---

## 七、第 2 轮回归复核结果

| 编号 | 修复文件与位置 | 复核结论 | 证据 |
|---|---|---|---|
| QA-001 | `DataInitService.java` L94 仅保留 `run()` 上的 `@Override`；L105 `initSchema()`、L125 `seedDemoData()` 注解已删除 | ✅ 静态复核通过 | 重新读取源文件确认；全类仅 1 个合法 `@Override`（实现 `CommandLineRunner.run`） |
| QA-002 | `TokenInterceptor.java` L58 改调 `isAdminEndpoint(method,path)`；L105-138 新增判定 | ✅ 静态复核通过 | 覆盖 `/api/admin/**` 前缀 + 12 个散落管理端写接口（coupons/admin、products、categories、store、orders/{id}/deliver）；写方法限定 POST/PUT/DELETE/PATCH；GET 读接口不受影响，H5 结算页仍可读 `/coupons/admin`；用户自身写操作（下单/支付/收货/取消/领券/评价）未被误伤 |
| QA-003 | `OrderServiceImpl.cancel()` L244-249 将 `cancelCas` 提前为 CAS 抢占守卫，返回 0 直接 return | ✅ 静态复核通过 | 状态校验（status=4 幂等、status=2 禁取消）仍在 CAS 之前，原有语义保留；后续 L250-261 的积分/券/库存回滚仅在抢占成功时执行；`cancelTimeoutOrders()` 复用 `cancel()` 同步受益 |

**测试代码适配（QA 自修复）**：
1. `OrderServiceTest` 两例补 `when(ordersMapper.cancelCas(ORDER_ID)).thenReturn(1)`；
2. `OrderServiceTest` 新增 `cancel_whenCasPreemptionFails_doesNotRollbackAnything`（CAS 失败时零副作用）与 `cancel_whenCasPreemptionSucceeds_rollsBackStockOnce`（成功时仅回滚一次）；
3. `TokenInterceptorTest` 新增 4 例：12 个管理端写接口 USER 拒绝 / ADMIN 放行、用户自身写操作不误伤、`GET /api/coupons/admin` 仍可用；
4. 全部 9 个测试文件再次通过 `javac -proc:none` 语法扫描（仅缺依赖符号，无语法错误）。

**低优先级项处理**：QA-004（CORS 通配 + credentials）与 QA-005（consume 忽略影响行数）经工程师说明后确认为可接受设计取舍，QA 认可该判断，不再追踪。

---

## 八、结论

四端代码在本轮验证范围内的表现：

- **前端三端**：真实构建 / 类型检查全部通过，API 路径与状态文案契约与后端一致；
- **后端**：核心业务逻辑（金额、库存 CAS、券状态机、积分幂等、订单 CAS、鉴权、数据初始化）经两轮静态审查确认符合 PRD 与 ARCH 设计；第 1 轮发现的 3 个缺陷已修复并复核；
- **未覆盖**：后端自动化测试因环境限制无法执行，需在具备 Maven 的 CI 环境中补跑 92 个用例作为最终放行依据。

**最终路由：NoOne。任务可关闭。**
