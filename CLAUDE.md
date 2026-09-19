# CLAUDE.md

> 这个文件是本项目的上下文说明，Claude Code 每次会话都会读取。请在开发中同步更新「当前进度」一节。

## 项目概览

前后端分离的**个人博客系统**，从零独立开发，目标是做成一个「能跑、能演示、每个技术点都能讲清楚」的作品集项目。

- 功能范围：用户注册登录 → 文章发布/编辑/删除 → 分类与标签 → 分页检索 → 评论互动 → 后台管理
- 不做的事：不做多租户、不做富文本协同编辑、不做全文检索引擎（用 MySQL LIKE 即可）

## 技术栈

| 层     | 选型                                      | 备注                                            |
| ------ | ----------------------------------------- | ----------------------------------------------- |
| 后端   | Spring Boot 3.x（Java 17）                | 本机 JDK 25，编译目标 17                        |
| 持久层 | **MyBatis + mybatis-spring-boot-starter** | **不要引入 MyBatis-Plus**，SQL 要手写，面试会问 |
| 数据库 | MySQL 8.0                                 | 本机已安装，库名 `blog_db`                      |
| 鉴权   | JWT（jjwt）                               | 登录签发 token，拦截器统一校验                  |
| 构建   | Maven                                     | 本机 3.9.16                                     |
| 前端   | Vue 3 + Vite + Axios + Vue Router         | 本机 Node 25 / npm 11                           |
| UI 库  | Element Plus                              | 可选，用于加快后台管理页面开发                  |

## 目录结构

```
blog-system/
├── backend/                                  # Spring Boot 后端
│   ├── src/main/java/com/sunyongjie/blog/
│   │   ├── BlogApplication.java
│   │   ├── controller/                       # 只做参数校验 + 调用 service
│   │   ├── service/                          # 业务逻辑（接口 + impl）
│   │   ├── mapper/                           # MyBatis Mapper 接口
│   │   ├── entity/                           # 与数据库表一一对应
│   │   ├── dto/                              # 请求 / 响应对象
│   │   ├── common/                           # Result、BizException、常量
│   │   ├── config/                           # 拦截器、跨域、MyBatis 配置
│   │   └── util/                             # JwtUtil 等
│   ├── src/main/resources/
│   │   ├── mapper/                           # MyBatis XML（SQL 写在这里）
│   │   ├── application.yml
│   │   └── schema.sql                        # 建表 + 初始数据
│   └── pom.xml
├── frontend/                                 # Vue 3 前端
│   ├── src/
│   │   ├── api/                              # axios 实例 + 接口定义
│   │   ├── router/
│   │   ├── views/
│   │   ├── components/
│   │   └── main.js
│   └── package.json
├── docs/
│   ├── requirements.md                       # 功能需求与验收标准
│   ├── database.md                           # 表结构说明
│   └── api.md                                # 接口清单
└── README.md
```

## 开发规范（重要）

1. **统一响应体**：所有接口返回 `Result<T> { code, message, data }`，不要直接返回实体类
2. **全局异常处理**：`@RestControllerAdvice` 捕获 `BizException`（业务异常）和 `Exception`（兜底），返回对应的 `Result`
3. **分层边界**：Controller 不写业务逻辑；Mapper 不做业务判断；事务加在 Service 层
4. **SQL 手写**：简单查询注解或 XML 皆可，**多条件查询必须用 XML 动态 SQL**（`<where>` / `<if>` / `<foreach>`），这是简历上写明的能力点
5. **命名**：数据库字段下划线、Java 驼峰，`map-underscore-to-camel-case: true`
6. **时间**：统一用 `LocalDateTime`；JDBC URL 带 `serverTimezone=Asia/Shanghai`
7. **密码**：存 BCrypt 哈希，**绝不存明文**；不要把数据库密码提交到 git（本地配置放 `application-local.yml`，已在 .gitignore 中）
8. **跨域**：开发环境用全局 CORS 配置，允许 `http://localhost:5173`

## 常用命令

```bash
# 后端（在 backend/ 下）
mvn clean spring-boot:run                 # 启动，默认 http://localhost:8080
mvn clean package -DskipTests             # 打包
mvn test                                  # 跑测试

# 数据库
mysql -u root -p < backend/src/main/resources/schema.sql

# 前端（在 frontend/ 下）
npm install
npm run dev                               # 默认 http://localhost:5173
npm run build
```

## 当前进度

- [x] 阶段 0：MySQL 建库、确认 Java/Maven/Node 版本
- [x] 阶段 1：后端骨架（pom、application.yml、启动类、统一响应体、全局异常）
- [x] 阶段 2：数据库表 + 实体 + Mapper
- [x] 阶段 3：注册登录 + JWT 鉴权 + 拦截器
- [x] 阶段 4：文章 CRUD + 分类标签 + 分页检索
- [x] 阶段 5：评论功能
- [x] 阶段 6：前端页面（列表 / 详情 / 编辑 / 登录 / 后台）
- [x] 阶段 7：前后端联调 + README 补充截图

> 每完成一项就把 `[ ]` 改成 `[x]`，并在下方追加一行变更记录。

### 待办 / 说明

- **阶段 7 的「README 截图」未做**：功能与接口已全部跑通（验收脚本 50 项全过），
  但 README 里的界面截图还没补，需要手工启动前后端后截图。
- **数据库连接**：`backend/src/main/resources/application-local.yml` 已在 `.gitignore` 中，
  换机器时从 `.example` 复制一份并填自己的 MySQL 密码即可。

### 验收方式

`scripts/verify-api.sh` 会对运行中的后端跑 50 项接口断言：
动态 SQL 拼接、分页、草稿隔离、401/403、越权、两级评论、参数校验、404 兜底。

```bash
cd backend && mvn spring-boot:run        # 另开一个终端
bash scripts/verify-api.sh               # PASS: 50  FAIL: 0
```

> 依赖 `curl` 与 `node`（用 node 解析 JSON 并按 UTF-8 写请求体，规避 Windows Git Bash
> 把命令行中文按 GBK 送出去的问题）。脚本会真的往库里写数据，跑完想恢复干净状态就重新执行一次
> `schema.sql`。

### 变更记录

- 2026-09-19：后端骨架。`pom.xml`（Spring Boot 3.5.16 / `<java.version>17</java.version>` /
  mybatis-spring-boot-starter 3.0.5 / jjwt 0.12.7 / spring-security-crypto / Lombok + annotationProcessorPaths）、
  `BlogApplication`（`@MapperScan`）、`common/`（`Result` / `ResultCode` / `BizException` / `PageResult` /
  `PageQuery` / `CurrentUser` / `UserContext` / `GlobalExceptionHandler`）、`util/JwtUtil`
  （jjwt 0.12.7 新 API，构造时校验 secret 至少 32 字节，否则直接启动失败）、
  `config/`（`AuthInterceptor` 读 `@RequireLogin` / `@RequireAdmin`，`afterCompletion` 清理 ThreadLocal；
  `WebMvcConfig` 注册拦截器 + CORS；`JacksonConfig` 注册 LocalDateTime 的 `yyyy-MM-dd HH:mm:ss` 序列化）。
- 2026-09-19：数据库与持久层。`schema.sql`（三张表 + 索引 + 初始数据：2 用户 / 3 分类 / 6 标签 /
  5 篇文章含 1 篇草稿 / 4 条评论含 1 条回复）、`entity/`、`dto/`、`mapper/` 接口与 `resources/mapper/*.xml`。
  多条件查询用 `<where>` + `<if>` 动态 SQL，标签批量插入用 `<foreach>`，全部 `#{}` 预编译占位。
- 2026-09-19：业务模块。`ArticleServiceImpl`（公开列表强制 `status=已发布`；`mine` 按当前用户过滤；
  详情对非作者的草稿返回 404；增删改校验归属，越权 403；删除级联清理评论与标签关联）、
  `CommentServiceImpl`（内存中拼两级结构避免 N+1；回复「回复」时拍平到顶层父评论；评论作者与文章作者都可删）、
  `UserServiceImpl`（注册用唯一索引兜底 `DuplicateKeyException`；登录对「用户不存在」和「密码错误」
  返回同一句提示，不泄露账号是否存在）。
- 2026-09-19：前端。Vue 3 + Vite + Element Plus + vue-router 4 + axios（不引 Pinia，用轻量 reactive store）；
  `request.js` 统一带 Bearer、解包 `Result`、401 清登录态并带 `redirect` 跳登录页；
  路由懒加载 + 守卫；页面：首页（分类筛选 + 关键词搜索 + 分页）、文章详情（两级评论、回复、按权限显示删除）、
  编辑器（存草稿 / 发布）、登录注册、文章管理、分类管理。
- 2026-09-19：验收结果。后端 `mvn spring-boot:run` 启动正常；`verify-blog.sh` **50 项断言全部通过**，
  覆盖动态 SQL（无条件时不产生多余 `WHERE`/`AND`、多条件正确 `AND` 拼接）、分页、草稿隔离（非作者 404）、
  401/403 鉴权与越权、两级评论、参数校验、404 兜底；前端 `npm run build` 退出码 0。
