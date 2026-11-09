# U易（Uyi）

U易是一个基于 Spring Boot 的后端服务，用于对接“U易”相关业务接口，提供验证码登录、设备查询以及热水设备启停等功能。项目采用 Java 17、Spring Boot、MyBatis 和 MySQL 构建，适合用于个人学习、功能扩展和二次开发。

> 请仅在获得授权的账号、设备和服务范围内使用本项目，并遵守相关服务条款及法律法规。

## 功能

- 手机号验证码发送
- 手机号与验证码登录
- 查询用户关联设备
- 开启热水设备
- 关闭热水设备并返回消费信息
- 用户登录信息和设备订单信息持久化
- 支持 Maven 和 Docker Compose 部署

## 技术栈

- Java 17
- Spring Boot 3.5.7
- Spring Web
- MyBatis
- MySQL
- OkHttp SSE
- Lombok
- Docker / Docker Compose

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+，或使用项目自带的 `./mvnw`
- MySQL 8+
- Git

### 1. 克隆项目

```bash
git clone https://github.com/qdd-y/Uyi.git
cd Uyi
```

### 2. 准备数据库

项目默认使用 MySQL 数据库 `youyi`，需要创建以下两张表：

```sql
CREATE DATABASE IF NOT EXISTS youyi DEFAULT CHARACTER SET utf8mb4;
USE youyi;

CREATE TABLE user_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(128) NOT NULL,
    token VARCHAR(512) NOT NULL,
    tel VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE equipment_order (
    id BIGINT PRIMARY KEY,
    order_num VARCHAR(128) NOT NULL,
    table_name VARCHAR(128),
    query_id VARCHAR(128),
    user_id VARCHAR(128) NOT NULL,
    token VARCHAR(512),
    device_inf_id VARCHAR(128) NOT NULL,
    created_at BIGINT
);
```

字段长度可根据实际接口返回值调整。应用会在登录成功后保存或更新用户信息，在开启设备后保存设备订单信息。

### 3. 配置数据库

编辑 `src/main/resources/application.yml`，配置数据库地址、端口、用户名和密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/youyi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8
    username: your_username
    password: your_password
```

生产环境请使用环境变量、配置中心或容器密钥管理数据库凭据，不要将真实密码提交到代码仓库。

### 4. 启动服务

```bash
./mvnw spring-boot:run
```

Windows 用户可以执行：

```bat
mvnw.cmd spring-boot:run
```

服务默认监听 `8082` 端口：<http://localhost:8082>

## API 接口

接口默认返回统一的 `Result` 结构。请求参数目前使用 Spring MVC 的查询参数绑定。

### 发送验证码

```http
POST /verifyCode?tel=手机号
```

### 登录

```http
GET /login?tel=手机号&code=验证码
```

登录成功后，服务会保存或更新该手机号对应的用户标识和令牌。

### 获取设备列表

```http
GET /get/device?tel=手机号
```

### 开启热水

```http
GET /open/hot?tel=手机号&deviceInfId=设备ID&billType=2
```

`billType=2` 表示热水；其他计费类型请以目标服务接口定义为准。

### 关闭热水

```http
GET /close/hot?tel=手机号&deviceInfId=设备ID
```

关闭成功后会返回消费金额等信息，并清理对应的设备订单记录。

## Docker 部署

先构建项目：

```bash
./mvnw clean package -DskipTests
```

然后构建镜像并启动：

```bash
docker build -t uyi-app .
docker compose up -d
```

默认映射端口为 `8082:8082`。当前 `docker-compose.yml` 使用外部 Docker 网络 `software_my-network`，启动前请确保该网络已经存在：

```bash
docker network create software_my-network
```

同时请确保容器内的数据库连接地址能够访问 MySQL 服务；推荐通过环境变量或挂载配置文件覆盖默认数据库配置。

## 项目结构

```text
src/main/java/com/example
├── UYiApplication.java
└── uyi
    ├── controller       # HTTP 接口
    ├── entity           # 请求、响应及数据实体
    ├── exception        # 异常处理
    ├── mapper           # MyBatis Mapper
    └── service          # 业务逻辑和第三方接口调用

src/main/resources
├── application.yml      # 应用配置
└── mapper               # MyBatis XML 映射文件
```

## 开发说明

- 默认服务端口为 `8082`。
- MyBatis Mapper 文件位于 `src/main/resources/mapper/`。
- 跨域请求已在控制器层启用，生产环境建议按实际前端域名收紧跨域范围。
- 对外部接口的请求依赖有效的用户登录信息和设备信息。

## License

当前仓库未声明开源许可证。若要公开分发或用于商业用途，请先补充合适的许可证，并确认第三方接口使用权限。
