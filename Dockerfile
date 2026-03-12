# 多阶段构建 - 前端构建阶段
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

# 复制前端依赖文件
COPY front/app/package*.json ./app/
COPY front/app/ ./app/

# 安装依赖并构建前端
WORKDIR /app/frontend/app
RUN npm ci && npm run build

# ==========================================
# 后端构建阶段
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /app

# 复制后端代码
COPY backend/pom.xml .
COPY backend/src ./src
COPY backend/lib ./lib

# 构建后端（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests -B

# ==========================================
# 运行阶段
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="campus-order"
LABEL description="校园点餐管理系统"

# 安装必要的工具
RUN apk add --no-cache tzdata curl

# 设置时区
ENV TZ=Asia/Shanghai

WORKDIR /app

# 从构建阶段复制文件
COPY --from=backend-builder /app/target/campus-order.jar app.jar
COPY --from=frontend-builder /app/frontend/app/dist ./static

# 创建必要的目录
RUN mkdir -p /app/uploads /var/log/campus-order

# 暴露端口
EXPOSE 9090

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9090/actuator/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# 设置运行用户（安全最佳实践）
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
