# tfzttest

这是一个 Spring Boot 微服务项目，用于从 Facebook 广告账户拉取广告数据。

## 目录结构
- `ad-data-service/`: 广告数据服务模块
- `eureka-server/`: Eureka 服务注册中心模块

## 安装与运行
1. 确保安装了 Java 17 和 Maven。
2. 设置环境变量 `FACEBOOK_ACCESS_TOKEN` 为有效的 Facebook 访问令牌。
3. 进入 `tfzttest/` 根目录，运行 `mvn clean install` 构建项目。
4. 启动 `eureka-server`: `cd eureka-server && mvn spring-boot:run`
5. 启动 `ad-data-service`: `cd ad-data-service && mvn spring-boot:run`
6. 访问 `http://localhost:8761` 确认 Eureka 运行。
7. 使用 Postman 测试 API: `GET http://localhost:8080/ads/1063250515828486`

## 依赖
- Spring Boot 3.3.4
- Spring Cloud 2023.0.3
- facebook-java-business-sdk 23.0.0