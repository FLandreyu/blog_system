package com.sunyongjie.blog.common;

/**
 * 业务状态码，与 docs/api.md 的「状态码约定」一一对应。
 */
public final class ResultCode {

    /** 成功 */
    public static final int SUCCESS = 200;
    /** 参数错误 */
    public static final int BAD_REQUEST = 400;
    /** 未登录 / token 无效 */
    public static final int UNAUTHORIZED = 401;
    /** 无权限（比如改了别人的文章） */
    public static final int FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int NOT_FOUND = 404;
    /** 服务器内部错误 */
    public static final int ERROR = 500;

    private ResultCode() {
    }
}
