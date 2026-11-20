@echo off
setlocal
set "ROOT=%~dp0.."
set "MVN="

rem 探测 Maven：PATH 优先，其次本机常用安装位置。
for /f "delims=" %%i in ('where mvn 2^>nul') do if not defined MVN set "MVN=%%i"
if not defined MVN if exist "%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd" set "MVN=%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd"
if not defined MVN (
    echo [FAIL] Maven not found. Set the MVN environment variable to the full path of mvn.cmd.
    exit /b 1
)

echo ==^> [1/4] Backend: mvn test
pushd "%ROOT%\springboot-supermarket" || exit /b 1
call "%MVN%" -B -ntp test
if errorlevel 1 goto failed
popd

rem 默认只做类型检查（无文件删除）；完整打包请设置环境变量 FULL_BUILD=1。
set "FE_SCRIPT=typecheck"
if "%FULL_BUILD%"=="1" set "FE_SCRIPT=build"

echo ==^> [2/4] H5: npm install ^& npm run %FE_SCRIPT%
pushd "%ROOT%\supermarket-h5" || exit /b 1
call npm install --no-audit --no-fund
if errorlevel 1 goto failed
call npm run %FE_SCRIPT%
if errorlevel 1 goto failed
popd

echo ==^> [3/4] Admin: npm install ^& npm run %FE_SCRIPT%
pushd "%ROOT%\supermarket-web-manager" || exit /b 1
call npm install --no-audit --no-fund
if errorlevel 1 goto failed
call npm run %FE_SCRIPT%
if errorlevel 1 goto failed
popd

echo ==^> [4/4] Mini-program: npm install ^& npm run typecheck
pushd "%ROOT%\supermarket-wechat" || exit /b 1
call npm install --no-audit --no-fund
if errorlevel 1 goto failed
call npm run typecheck
if errorlevel 1 goto failed
popd

echo ALL CHECKS PASSED
exit /b 0

:failed
echo [FAIL] Quality gate failed.
popd
exit /b 1
