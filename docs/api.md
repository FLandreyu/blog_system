# 接口清单

- 统一前缀：`/api`
- 统一响应：`{ "code": 200, "message": "ok", "data": ... }`
- 鉴权：除注册/登录外，写操作都需要请求头 `Authorization: Bearer <token>`
- 分页响应统一为：`{ "total": 100, "page": 1, "size": 10, "list": [] }`

## 状态码约定

| code | 含义                         |
| ---- | ---------------------------- |
| 200  | 成功                         |
| 400  | 参数错误                     |
| 401  | 未登录 / token 无效          |
| 403  | 无权限（比如改了别人的文章） |
| 404  | 资源不存在                   |
| 500  | 服务器内部错误               |

## 1. 认证

| 方法 | 路径                 | 说明             | 鉴权 |
| ---- | -------------------- | ---------------- | ---- |
| POST | `/api/auth/register` | 注册             | 否   |
| POST | `/api/auth/login`    | 登录，返回 token | 否   |
| GET  | `/api/auth/me`       | 当前登录用户信息 | 是   |

**POST /api/auth/login**

```json
// 请求
{ "username": "admin", "password": "123456" }

// 响应
{
  "code": 200,
  "message": "ok",
  "data": {
    "token": "eyJhbGciOi...",
    "user": { "id": 1, "username": "admin", "nickname": "站长", "role": "ADMIN" }
  }
}
```

## 2. 文章

| 方法   | 路径                 | 说明                                                | 鉴权 |
| ------ | -------------------- | --------------------------------------------------- | ---- |
| GET    | `/api/articles`      | 分页列表，支持 `page` `size` `categoryId` `keyword` | 否   |
| GET    | `/api/articles/{id}` | 详情，浏览量 +1                                     | 否   |
| POST   | `/api/articles`      | 发布文章                                            | 是   |
| PUT    | `/api/articles/{id}` | 编辑（仅作者）                                      | 是   |
| DELETE | `/api/articles/{id}` | 删除（仅作者）                                      | 是   |
| GET    | `/api/articles/mine` | 我的文章（含草稿）                                  | 是   |

**GET /api/articles?page=1&size=10&categoryId=1&keyword=Spring**

```json
{
  "code": 200,
  "message": "ok",
  "data": {
    "total": 23,
    "page": 1,
    "size": 10,
    "list": [
      {
        "id": 1,
        "title": "MyBatis 动态 SQL 实践",
        "summary": "记录 where / if / foreach 的用法",
        "categoryName": "技术笔记",
        "authorName": "站长",
        "viewCount": 42,
        "createTime": "2026-09-01 10:20:30"
      }
    ]
  }
}
```

> **实现要求**：`categoryId` 与 `keyword` 都为空时不能拼出多余条件，用 `<where>` + `<if>` 处理。

## 3. 分类与标签

| 方法   | 路径                   | 说明               | 鉴权        |
| ------ | ---------------------- | ------------------ | ----------- |
| GET    | `/api/categories`      | 分类列表           | 否          |
| POST   | `/api/categories`      | 新增分类           | 是（ADMIN） |
| PUT    | `/api/categories/{id}` | 修改               | 是（ADMIN） |
| DELETE | `/api/categories/{id}` | 删除，被引用时拒绝 | 是（ADMIN） |
| GET    | `/api/tags`            | 标签列表           | 否          |

## 4. 评论

| 方法   | 路径                        | 说明             | 鉴权 |
| ------ | --------------------------- | ---------------- | ---- |
| GET    | `/api/comments?articleId=1` | 某文章的评论列表 | 否   |
| POST   | `/api/comments`             | 发表评论         | 是   |
| DELETE | `/api/comments/{id}`        | 删除评论         | 是   |

## 联调检查表

后端每写完一个接口，用 curl 验证一遍再写下一个：

```bash
# 登录拿 token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 带 token 调写接口
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"title":"测试","content":"正文","categoryId":1,"status":1}'

# 不带 token 应该返回 401
curl -X POST http://localhost:8080/api/articles -H "Content-Type: application/json" -d '{}'
```
