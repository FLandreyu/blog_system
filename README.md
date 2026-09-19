# 个人博客系统

基于 **Spring Boot + MyBatis + MySQL + Vue 3** 的前后端分离博客系统，支持文章发布、分类检索与评论互动。

## 功能

- 用户注册 / 登录，JWT 签发与校验，接口权限拦截
- 文章发布、编辑、删除、草稿与发布状态
- 分类与标签管理，文章按分类筛选
- 文章列表分页、关键词搜索
- 评论发表与列表展示
- 后台管理页面（文章管理、分类管理）
- 统一响应结构与全局异常处理

## 技术栈

| 层     | 技术                                         |
| ------ | -------------------------------------------- |
| 后端   | Java 17、Spring Boot 3.x、MyBatis、Maven     |
| 数据库 | MySQL 8.0                                    |
| 鉴权   | JWT（jjwt）                                  |
| 前端   | Vue 3、Vite、Vue Router、Axios、Element Plus |

> 持久层用的是 **MyBatis（手写 SQL）**，没有引入 MyBatis-Plus：
> 多条件检索用 XML 动态 SQL（`<where>` / `<if>` / `<foreach>`）实现，所有参数走 `#{}` 预编译占位。

## 快速开始

### 1. 准备数据库

```bash
mysql -u root -p < backend/src/main/resources/schema.sql
```

### 2. 启动后端

```bash
cd backend
# 复制配置模板并填入自己的数据库密码
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
mvn spring-boot:run
```

后端默认运行在 <http://localhost:8080>。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 <http://localhost:5173>，已配置代理到后端。

### 4. 初始账号

`schema.sql` 会写入两个演示账号，密码都是 `123456`（库里存的是 BCrypt 哈希，不是明文）：

| 用户名  | 密码     | 角色   | 说明                       |
| ------- | -------- | ------ | -------------------------- |
| `admin` | `123456` | ADMIN  | 可管理分类，另有 1 篇草稿  |
| `user`  | `123456` | 普通   | 可用于验证越权与草稿隔离   |

## 接口验收

后端启动后，在项目根目录执行：

```bash
bash scripts/verify-api.sh
```

脚本会对 **50 项**接口行为做断言（动态 SQL 拼接、分页、草稿隔离、401/403 越权、两级评论、
参数校验、404 兜底），全部通过时打印 `PASS: 50  FAIL: 0`。

## 文档

- [功能需求与验收标准](docs/requirements.md)
- [数据库设计](docs/database.md)
- [接口清单](docs/api.md)

## 目录结构

```
blog-system/
├── backend/     # Spring Boot 后端
├── frontend/    # Vue 3 前端
├── docs/        # 设计文档
└── CLAUDE.md    # 开发说明（供 AI 助手与协作者参考）
```
