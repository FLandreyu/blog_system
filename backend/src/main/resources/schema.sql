-- ============================================================
--  个人博客系统 建表脚本
--  执行：mysql -u root -p < backend/src/main/resources/schema.sql
--
--  注意：脚本最后会 DROP 并重建所有表，重复执行会清空数据。
-- ============================================================

CREATE DATABASE IF NOT EXISTS blog_db
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE blog_db;

-- 保证用命令行导入时中文不乱码
SET NAMES utf8mb4;

-- 有外键依赖的先删
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS article_tag;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS `user`;

-- ------------------------------------------------------------
-- user
-- ------------------------------------------------------------
CREATE TABLE `user` (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username    VARCHAR(50)  NOT NULL COMMENT '登录名，唯一',
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希，绝不存明文',
  nickname    VARCHAR(50)  NOT NULL COMMENT '昵称',
  email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱，可空',
  avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL，可空',
  role        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN / USER',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户';

-- ------------------------------------------------------------
-- category
-- ------------------------------------------------------------
CREATE TABLE category (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(50) NOT NULL COMMENT '分类名，唯一',
  sort        INT         NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='分类';

-- ------------------------------------------------------------
-- tag
-- ------------------------------------------------------------
CREATE TABLE tag (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(50) NOT NULL COMMENT '标签名，唯一',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='标签';

-- ------------------------------------------------------------
-- article
-- ------------------------------------------------------------
CREATE TABLE article (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  title       VARCHAR(200) NOT NULL COMMENT '标题',
  summary     VARCHAR(500) DEFAULT NULL COMMENT '摘要，可空',
  content     LONGTEXT     NOT NULL COMMENT '正文',
  cover       VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL，可空',
  category_id BIGINT       DEFAULT NULL COMMENT '关联 category.id，可空',
  author_id   BIGINT       NOT NULL COMMENT '关联 user.id',
  status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布',
  view_count  INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_category_id (category_id),
  KEY idx_author_id (author_id),
  KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='文章';

-- ------------------------------------------------------------
-- article_tag：文章与标签的多对多关联
--
-- 用 (article_id, tag_id) 联合主键而不是自增 id：
-- 关联表只表达"存在这条关系"，联合主键天然保证不重复，
-- 而且 InnoDB 的聚簇索引会把同一篇文章的标签物理放在一起，
-- 按 article_id 查标签时是一次顺序读。
-- ------------------------------------------------------------
CREATE TABLE article_tag (
  article_id BIGINT NOT NULL COMMENT '关联 article.id',
  tag_id     BIGINT NOT NULL COMMENT '关联 tag.id',
  PRIMARY KEY (article_id, tag_id),
  KEY idx_tag_id (tag_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='文章-标签关联';

-- ------------------------------------------------------------
-- comment
-- ------------------------------------------------------------
CREATE TABLE comment (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  article_id  BIGINT        NOT NULL COMMENT '关联 article.id',
  user_id     BIGINT        NOT NULL COMMENT '评论人',
  content     VARCHAR(1000) NOT NULL COMMENT '内容',
  parent_id   BIGINT        DEFAULT NULL COMMENT '父评论 id，顶层为 NULL',
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_article_id (article_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评论';

-- ============================================================
--  初始数据
--  两个演示账号的密码都是 123456，存的是 BCrypt 哈希
-- ============================================================
INSERT INTO `user` (id, username, password, nickname, email, role) VALUES
  (1, 'admin', '$2a$10$CsQgBZq8YKNnJqbpDSmJb.09paumFU12tKe7RNDYAiVFAWouLusAC', '站长', 'admin@example.com', 'ADMIN'),
  (2, 'user',  '$2a$10$CsQgBZq8YKNnJqbpDSmJb.09paumFU12tKe7RNDYAiVFAWouLusAC', '小明', 'user@example.com',  'USER');

INSERT INTO category (id, name, sort) VALUES
  (1, '技术笔记', 1),
  (2, '学习总结', 2),
  (3, '随笔', 3);

INSERT INTO tag (id, name) VALUES
  (1, 'Java'),
  (2, 'Spring Boot'),
  (3, 'MyBatis'),
  (4, 'MySQL'),
  (5, 'Vue'),
  (6, '前端');

INSERT INTO article (id, title, summary, content, category_id, author_id, status, view_count) VALUES
  (1, 'MyBatis 动态 SQL 实践：where / if / foreach',
      '记录 where、if、foreach 三个标签各自解决了什么问题。',
      '手写 SQL 时最容易踩的坑，是用字符串拼接条件。只要有一个条件为空，就会拼出 WHERE AND title LIKE ''%%'' 这种语法错误的 SQL。MyBatis 的 where 标签会自动去掉开头多余的 AND，并且当所有 if 都不成立时，整段 WHERE 子句不会生成。foreach 则用来把集合展开成 IN (...) 或者批量 INSERT 的多个 VALUES。配合 #{} 占位符，SQL 结构由标签决定，参数值走预编译，两者都不会有注入风险。',
      1, 1, 1, 42),
  (2, 'Spring Boot 统一响应与全局异常处理',
      '为什么所有接口都要返回同一个 Result 结构。',
      '前后端分离之后，前端最怕的是「同一个接口，成功时返回一种结构，失败时又是另一种」。统一响应体的价值在于把这件事收敛成一个契约：无论成功还是任何一种失败，前端拿到的永远是 code、message、data 三个字段。实现上是两层：业务代码遇到可预期的问题就抛 BizException，由 RestControllerAdvice 统一转成 Result；没预料到的异常走兜底分支，记完整堆栈、对外只给一句友好提示，不把堆栈暴露出去。',
      1, 1, 1, 88),
  (3, 'MySQL 索引为什么会失效',
      '从最左前缀到 LIKE 前置百分号。',
      '索引失效的几种典型场景：一是违反最左前缀原则，联合索引 (a, b) 上只用 b 查是用不上的；二是在索引列上做运算或调用函数，比如 WHERE YEAR(create_time) = 2026；三是 LIKE 以百分号开头，B+ 树没法从左边开始定位。还有一种容易被忽略：隐式类型转换。如果字段是 varchar，却用数字去比较，MySQL 会把字段转成数字，等价于在列上加了函数，索引同样用不上。',
      1, 2, 1, 130),
  (4, '我的 2026 学习计划',
      '把「学过」变成「能讲清楚」。',
      '过去一年看了不少东西，但真正能复述出来的不多。反思下来问题在于：输入太多、输出太少。今年的调整是每学一个知识点，强迫自己写一篇能给别人讲明白的笔记，并配一个可以跑起来的最小示例。判断标准很简单——如果面试官追问三层我就答不上来，那说明这个点其实还没学会。',
      2, 2, 1, 56),
  (5, 'Vue 3 组合式 API 入门笔记',
      'ref、reactive 与生命周期钩子的写法变化。',
      '组合式 API 最直接的变化是逻辑可以按功能组织，而不是被迫按选项分散在 data、methods、mounted 里。ref 用来包装基本类型，取值要写 .value；reactive 用于对象，直接访问属性即可。生命周期钩子变成 onMounted 这样的函数，在 setup 里调用就行。这篇还没写完，先存成草稿。',
      1, 1, 0, 3);

INSERT INTO article_tag (article_id, tag_id) VALUES
  (1, 2), (1, 3),
  (2, 2),
  (3, 4),
  (4, 1),
  (5, 5), (5, 6);

INSERT INTO comment (id, article_id, user_id, content, parent_id) VALUES
  (1, 1, 2, '正好在写这块，where 标签确实省心。', NULL),
  (2, 1, 1, '补充一点：if 里判断字符串建议同时判 null 和空串，否则前端传空字符串时条件还是会被拼进去。', 1),
  (3, 2, 2, '兜底异常那段讲得很清楚，之前一直没想明白为什么要单独留一个分支。', NULL),
  (4, 3, 1, '隐式类型转换这个坑我踩过，查了半天才发现是字段类型的问题。', NULL);
