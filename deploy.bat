@echo off
REM ============================================
REM 校园点餐系统部署脚本
REM ============================================

echo ========================================
echo 校园点餐系统 - 部署脚本
echo ========================================
echo.

REM 配置变量
set SERVER_HOST=39.106.46.109
set SERVER_USER=root
set SERVER_PASSWORD=147258369Thy@
set SERVER_DIR=/root/campus-order

echo [1/5] 检查本地打包文件...
if not exist "front\app\dist" (
    echo 错误: 前端dist目录不存在，请先运行 npm run build
    pause
    exit /b 1
)
if not exist "backend\target\campus-order-0.0.1-SNAPSHOT.jar" (
    echo 错误: 后端jar包不存在，请先运行 mvn clean package -DskipTests
    pause
    exit /b 1
)
echo.

echo [2/5] 创建临时部署目录...
echo 需要在服务器上执行以下命令:
echo   mkdir -p %SERVER_DIR%/front
echo   mkdir -p %SERVER_DIR%/backend
echo   mkdir -p %SERVER_DIR%/logs
echo.

echo [3/5] 上传前端文件...
echo 使用以下命令上传前端dist目录:
echo   scp -r front\app\dist\* %SERVER_USER%@%SERVER_HOST%:%SERVER_DIR%/front/
echo.

echo [4/5] 上传后端jar包...
echo 使用以下命令上传后端jar包:
echo   scp backend\target\campus-order-0.0.1-SNAPSHOT.jar %SERVER_USER%@%SERVER_HOST%:%SERVER_DIR%/backend/
echo.

echo [5/5] 启动服务...
echo SSH到服务器后执行:
echo   cd %SERVER_DIR%/backend
echo   nohup java -jar -Dspring.profiles.active=prod ^
echo     -DDB_URL=jdbc:mysql://localhost:3306/campus_order ^
echo     -DDB_PASSWORD=your_password ^
echo     -DREDIS_PASSWORD=your_redis_password ^
echo     campus-order-0.0.1-SNAPSHOT.jar > logs/app.log 2^>^&1 ^&
echo.

echo ========================================
echo 部署准备完成！
echo 请手动执行上述命令完成部署
echo ========================================
pause
