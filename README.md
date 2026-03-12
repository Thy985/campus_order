# 校园点餐管理系统

一个面向高校场景的在线订餐平台，支持用户、商家、管理员三种角色。

## 技术栈

### 后端
- Spring Boot 3.x
- MyBatis-Flex + MySQL
- Redis（缓存、分布式锁、WebSocket）
- Sa-Token（认证授权）
- Spring Doc OpenAPI（API文档）

### 前端
- React 18 + TypeScript
- Vite（构建工具）
- shadcn/ui（UI组件库）
- React Query（状态管理）
- Zustand（状态管理）

## 功能模块

### 用户端
- 用户注册/登录（手机号 + 验证码）
- 餐厅浏览、商品搜索
- 购物车、订单提交
- 支付宝支付（沙箱环境）
- 订单跟踪、评价

### 商户端
- 店铺管理、商品上下架
- 营业时间设置
- 订单处理
- 销售统计

### 管理端
- 用户管理、商户审核
- 订单管理
- 数据统计面板

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+

### 配置步骤

1. **克隆项目**
```bash
git clone https://gitee.com/star-expedition-guest/campus_order.git
cd campus_order
```

2. **配置后端**
```bash
# 复制环境变量示例文件
cp backend/.env.example backend/.env

# 编辑 .env 文件，配置数据库、Redis等
```

3. **初始化数据库**
```bash
# 使用 Docker 启动 MySQL 和 Redis
docker-compose -f docker-compose.dev.yml up -d
```

4. **启动后端**
```bash
cd backend
mvn spring-boot:run
```

5. **启动前端**
```bash
cd front/app
npm install
npm run dev
```

### 默认访问地址
- 前端：http://localhost:5173
- 后端API：http://localhost:9090
- API文档：http://localhost:9090/swagger-ui.html

### 测试账号
- 管理员：admin / admin123
- 商户：需要在管理端创建

## 项目结构

```
campus-order/
├── backend/                 # Spring Boot 后端
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Java 源代码
│   │       └── resources/ # 配置文件
│   └── pom.xml
├── front/app/              # React 前端
│   ├── src/
│   │   ├── api/           # API 调用
│   │   ├── components/    # 组件
│   │   └── hooks/         # 自定义 Hooks
│   └── package.json
├── docs/                  # 项目文档
└── docker-compose.dev.yml # Docker 配置
```

## API 文档

启动后端后访问：http://localhost:9090/swagger-ui.html

## 注意事项

1. 敏感信息已移除，首次使用请配置 `.env` 文件
2. 支付功能使用支付宝沙箱环境
3. 生产环境部署请修改相关配置

## 许可证

MIT License
