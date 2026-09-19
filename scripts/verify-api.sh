#!/bin/bash
# 博客系统接口验收脚本
#
# 注意：Windows Git Bash 会把 -d 里的中文按 GBK 编码送出去，导致后端 JSON 解析失败。
# 所以凡是含中文的请求体，一律先由 node 以 UTF-8 写文件，再用 --data-binary @file 发送。
BASE=http://localhost:8080/api
# 临时 JSON 存放目录。必须用 Windows 风格路径（D:/...）：node 和 curl 都是原生 Windows 程序，
# 认不出 Git Bash 的 /tmp、/d/... 这类路径，否则写文件会静默失败、请求体变空。
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WORK="$(cygpath -m "$ROOT" 2>/dev/null || echo "$ROOT")/.verify-work"
mkdir -p "$WORK"
PASS=0; FAIL=0

field() { node -e "let d='';process.stdin.on('data',c=>d+=c).on('end',()=>{try{const o=JSON.parse(d);const v=o$1;console.log(v===null?'null':(typeof v==='object'?JSON.stringify(v):v))}catch(e){console.log('PARSE_ERR')}})"; }

# mkbody <文件> <JS 对象字面量>
mkbody() { node -e "require('fs').writeFileSync('$1', JSON.stringify($2), 'utf8')"; }

check() { # 名称 期望 实际
  if [ "$2" = "$3" ]; then echo "  PASS  $1 = $3"; PASS=$((PASS+1));
  else echo "  FAIL  $1  期望=$2 实际=$3"; FAIL=$((FAIL+1)); fi
}

echo "=== 等待服务就绪 ==="
for i in $(seq 1 45); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/articles?page=1" 2>/dev/null)
  [ "$code" = "200" ] && { echo "服务已就绪 (HTTP 200)"; break; }
  sleep 2
done
[ "$code" != "200" ] && { echo "服务未就绪，中止"; exit 1; }

echo
echo "=== 1. 公开列表 / 动态 SQL ==="
R=$(curl -s "$BASE/articles?page=1&size=3")
check "只返回已发布(total)" 4 "$(echo "$R" | field '.data.total')"
check "分页 size 生效(list长度)" 3 "$(echo "$R" | field '.data.list.length')"
check "关键词模糊搜索 total" 1 "$(curl -s "$BASE/articles?keyword=MyBatis" | field '.data.total')"
check "分类筛选 total" 3 "$(curl -s "$BASE/articles?categoryId=1" | field '.data.total')"
check "关键词+分类组合 total" 1 "$(curl -s "$BASE/articles?categoryId=1&keyword=MySQL" | field '.data.total')"
check "条件全空不产生多余条件" 4 "$(curl -s "$BASE/articles?keyword=&categoryId=" | field '.data.total')"
check "草稿不出现在公开列表" 0 "$(curl -s "$BASE/articles?keyword=Vue" | field '.data.total')"

echo
echo "=== 2. 文章详情 ==="
D=$(curl -s "$BASE/articles/1")
check "详情 code" 200 "$(echo "$D" | field '.code')"
check "标签数量" 2 "$(echo "$D" | field '.data.tags.length')"
check "评论数" 2 "$(echo "$D" | field '.data.commentCount')"
check "作者名" "站长" "$(echo "$D" | field '.data.authorName')"
V1=$(echo "$D" | field '.data.viewCount')
V2=$(curl -s "$BASE/articles/1" | field '.data.viewCount')
check "浏览量自增" "$((V1+1))" "$V2"
check "不存在的文章 404" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/99999")"

echo
echo "=== 3. 鉴权 ==="
check "无 token 发文章 401" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/articles" -H 'Content-Type: application/json' -d '{}')"
check "无 token 查我的文章 401" 401 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/mine")"
check "伪造 token 401" 401 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/mine" -H 'Authorization: Bearer not.a.real.token')"
check "401 响应体仍是 Result" 401 "$(curl -s "$BASE/articles/mine" | field '.code')"
check "无 token 评论 401" 401 "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/comments" -H 'Content-Type: application/json' -d '{}')"

echo
echo "=== 4. 登录 ==="
L=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"123456"}')
check "登录 code" 200 "$(echo "$L" | field '.code')"
check "返回角色" "ADMIN" "$(echo "$L" | field '.data.user.role')"
ATOKEN=$(echo "$L" | field '.data.token')
check "密码错误被拒" 400 "$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":"wrong"}' | field '.code')"
check "参数校验(空密码) 400" 400 "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"admin","password":""}')"
check "不存在的用户不泄露差异" 400 "$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"nobody","password":"123456"}' | field '.code')"

UTOKEN=$(curl -s -X POST "$BASE/auth/login" -H 'Content-Type: application/json' -d '{"username":"user","password":"123456"}' | field '.data.token')
check "当前用户接口" "admin" "$(curl -s "$BASE/auth/me" -H "Authorization: Bearer $ATOKEN" | field '.data.username')"

echo
echo "=== 5. 我的文章（含草稿）与草稿隔离 ==="
check "admin 我的文章 total(含1篇草稿)" 3 "$(curl -s "$BASE/articles/mine" -H "Authorization: Bearer $ATOKEN" | field '.data.total')"
check "user 我的文章 total(仅自己2篇)" 2 "$(curl -s "$BASE/articles/mine" -H "Authorization: Bearer $UTOKEN" | field '.data.total')"
check "user 看不到 admin 的草稿" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/5" -H "Authorization: Bearer $UTOKEN")"
check "匿名看不到 admin 的草稿" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/5")"
check "作者本人可看自己的草稿" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/5" -H "Authorization: Bearer $ATOKEN")"

echo
echo "=== 6. 角色权限 ==="
check "普通用户建分类 403" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X POST "$BASE/categories" -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' -d '{}')"
check "403 响应体仍是 Result" 403 "$(curl -s -X DELETE "$BASE/categories/1" -H "Authorization: Bearer $UTOKEN" | field '.code')"
check "管理员删被引用分类 400" 400 "$(curl -s -X DELETE "$BASE/categories/1" -H "Authorization: Bearer $ATOKEN" | field '.code')"
check "分类列表" 3 "$(curl -s "$BASE/categories" | field '.data.length')"
check "分类文章数含草稿(技术笔记=4)" 4 "$(curl -s "$BASE/categories" | field '.data[0].articleCount')"

echo
echo "=== 7. 文章写操作与越权 ==="
mkbody "$WORK/a-create.json" '{title:"接口验收临时文章",content:"正文内容",categoryId:1,tagIds:[1,2],status:0}'
NEW=$(curl -s -X POST "$BASE/articles" -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' --data-binary @"$WORK/a-create.json")
check "创建文章 code" 200 "$(echo "$NEW" | field '.code')"
NID=$(echo "$NEW" | field '.data')
check "新文章默认草稿且不在公开列表" 0 "$(curl -s "$BASE/articles?keyword=%E6%8E%A5%E5%8F%A3%E9%AA%8C%E6%94%B6" | field '.data.total')"
check "无效标签被拒 400" 400 "$(curl -s -X POST "$BASE/articles" -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' -d '{"title":"x","content":"y","tagIds":[999]}' | field '.code')"

mkbody "$WORK/a-tamper.json" '{title:"篡改标题",content:"篡改内容"}'
check "user 改别人的文章 403" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$BASE/articles/1" -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' --data-binary @"$WORK/a-tamper.json")"
check "user 删别人的文章 403" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X DELETE "$BASE/articles/1" -H "Authorization: Bearer $UTOKEN")"
check "作者删自己的文章 200" 200 "$(curl -s -X DELETE "$BASE/articles/$NID" -H "Authorization: Bearer $UTOKEN" | field '.code')"
check "删除后详情 404" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/articles/$NID")"

echo
echo "=== 8. 评论 ==="
C=$(curl -s "$BASE/comments?articleId=1")
check "顶层评论数(两级结构)" 1 "$(echo "$C" | field '.data.length')"
check "回复挂在父评论下" 1 "$(echo "$C" | field '.data[0].replies.length')"
mkbody "$WORK/c-new.json" '{articleId:1,content:"验收测试评论"}'
CNEW=$(curl -s -X POST "$BASE/comments" -H "Authorization: Bearer $UTOKEN" -H 'Content-Type: application/json' --data-binary @"$WORK/c-new.json")
check "发表评论 code" 200 "$(echo "$CNEW" | field '.code')"
CID=$(echo "$CNEW" | field '.data')
check "评论数增加到 3" 3 "$(curl -s "$BASE/articles/1" | field '.data.commentCount')"
check "评论作者可删自己的" 200 "$(curl -s -X DELETE "$BASE/comments/$CID" -H "Authorization: Bearer $UTOKEN" | field '.code')"
check "删后评论数回到 2" 2 "$(curl -s "$BASE/articles/1" | field '.data.commentCount')"
check "文章作者可删他人评论 200" 200 "$(curl -s -X DELETE "$BASE/comments/3" -H "Authorization: Bearer $ATOKEN" | field '.code')"

echo
echo "=== 9. 其他 ==="
check "标签列表" 6 "$(curl -s "$BASE/tags" | field '.data.length')"
check "未知路径 404" 404 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE/nope")"

echo
echo "================================"
echo "  PASS: $PASS    FAIL: $FAIL"
echo "================================"
[ "$FAIL" -gt 0 ] && exit 1
exit 0
