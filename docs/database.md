# 数据库设计

- 数据库名：`blog_db`
- 字符集：`utf8mb4` / `utf8mb4_general_ci`
- 引擎：InnoDB
- 时间统一用 `datetime`，Java 侧用 `LocalDateTime`

## 表清单

| 表名          | 说明                    |
| ------------- | ----------------------- |
| `user`        | 用户                    |
| `category`    | 分类                    |
| `tag`         | 标签                    |
| `article`     | 文章                    |
| `article_tag` | 文章-标签关联（多对多） |
| `comment`     | 评论                    |

## 字段说明

### user

| 字段        | 类型               | 说明                          |
| ----------- | ------------------ | ----------------------------- |
| id          | bigint PK AI       |                               |
| username    | varchar(50) UNIQUE | 登录名，唯一                  |
| password    | varchar(100)       | BCrypt 哈希，**不存明文**     |
| nickname    | varchar(50)        | 昵称                          |
| email       | varchar(100)       | 可空                          |
| avatar      | varchar(255)       | 头像 URL，可空                |
| role        | varchar(20)        | `ADMIN` / `USER`，默认 `USER` |
| create_time | datetime           |                               |
| update_time | datetime           |                               |

### article

| 字段        | 类型         | 说明                   |
| ----------- | ------------ | ---------------------- |
| id          | bigint PK AI |                        |
| title       | varchar(200) | 标题                   |
| summary     | varchar(500) | 摘要，可空             |
| content     | longtext     | 正文                   |
| cover       | varchar(255) | 封面图，可空           |
| category_id | bigint       | 关联 category.id，可空 |
| author_id   | bigint       | 关联 user.id           |
| status      | tinyint      | 0=草稿 1=已发布        |
| view_count  | int          | 浏览量，默认 0         |
| create_time | datetime     |                        |
| update_time | datetime     |                        |

索引：`idx_category_id`、`idx_author_id`、`idx_create_time`

### category

| 字段        | 类型         | 说明           |
| ----------- | ------------ | -------------- |
| id          | bigint PK AI |                |
| name        | varchar(50)  | 分类名，唯一   |
| sort        | int          | 排序值，默认 0 |
| create_time | datetime     |                |

### tag / article_tag

`tag(id, name, create_time)`；`article_tag(article_id, tag_id)` 联合主键，双向外键索引。

### comment

| 字段        | 类型          | 说明                   |
| ----------- | ------------- | ---------------------- |
| id          | bigint PK AI  |                        |
| article_id  | bigint        | 关联文章               |
| user_id     | bigint        | 评论人                 |
| content     | varchar(1000) | 内容                   |
| parent_id   | bigint        | 父评论 id，顶层为 NULL |
| create_time | datetime      |                        |

索引：`idx_article_id`

## 建表脚本

写入 `backend/src/main/resources/schema.sql`，用如下形式（**执行前确认库已创建**）：

```sql
CREATE DATABASE IF NOT EXISTS blog_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE blog_db;

DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS article_tag;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(50)  NOT NULL COMMENT '登录名',
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希',
  nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
  email       VARCHAR(100) DEFAULT NULL,
  avatar      VARCHAR(255) DEFAULT NULL,
  role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN/USER',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- article / category / tag / article_tag / comment 依此类推，字段按上面表格补齐

-- 初始数据：默认分类 + 管理员账号（密码用 BCrypt 生成后替换，不要写明文）
INSERT INTO category (name, sort) VALUES ('技术笔记', 1), ('学习总结', 2), ('随笔', 3);
```

## 待思考的问题（面试会问）

1. `article_tag` 为什么要用联合主键，而不是自增 id？
2. 删除分类时被文章引用，应该怎么做？（禁止删除 / 置 NULL / 级联）
3. `view_count` 每次访问都 `UPDATE` 会有什么问题？
4. 评论的两级回复为什么不做成无限层级？
5. 哪些字段需要建索引？`title LIKE '%关键词%'` 能走索引吗？
