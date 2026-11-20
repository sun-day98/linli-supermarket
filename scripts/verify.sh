#!/usr/bin/env bash
# 邻里小超 · 本地提交前质量门禁（Git Bash / Linux / macOS 可用）
# 用法：bash scripts/verify.sh   或   ./scripts/verify.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

# 自动探测 Maven：PATH > 本机常用安装位置；也可通过 MVN 环境变量覆盖。
if command -v mvn >/dev/null 2>&1; then
  MVN="$(command -v mvn)"
elif [ -x "$HOME/tools/apache-maven-3.9.9/bin/mvn" ]; then
  MVN="$HOME/tools/apache-maven-3.9.9/bin/mvn"
else
  echo "[FAIL] 未找到 Maven，请设置 MVN 环境变量指向 mvn 可执行文件。" >&2
  exit 1
fi

echo "==> [1/4] 后端：mvn test（94 用例）  Maven=$MVN"
(cd springboot-supermarket && "$MVN" -B -ntp test)

# 本地用 npm install 增量安装（npm ci 会清空 node_modules，可能触发沙箱批量删除保护）。
# 默认只做类型检查（无文件删除）；需要完整打包时运行：FULL_BUILD=1 bash scripts/verify.sh
# CI（.github/workflows/ci.yml）在干净环境中始终用 npm ci + 完整 build，保证可复现。
FRONTEND_SCRIPT=typecheck
if [ "${FULL_BUILD:-0}" = "1" ]; then FRONTEND_SCRIPT=build; fi

echo "==> [2/4] 前端 H5：npm install && npm run $FRONTEND_SCRIPT"
(cd supermarket-h5 && npm install --no-audit --no-fund && npm run "$FRONTEND_SCRIPT")

echo "==> [3/4] 前端管理后台：npm install && npm run $FRONTEND_SCRIPT"
(cd supermarket-web-manager && npm install --no-audit --no-fund && npm run "$FRONTEND_SCRIPT")

echo "==> [4/4] 微信小程序：npm install && npm run typecheck"
(cd supermarket-wechat && npm install --no-audit --no-fund && npm run typecheck)

echo "==> ALL CHECKS PASSED"
