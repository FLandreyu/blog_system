package com.sunyongjie.blog.common;

/**
 * 当前登录人的 ThreadLocal 容器。
 *
 * <p>由 AuthInterceptor 在 preHandle 里写入、afterCompletion 里清除。
 * <b>必须清除</b>：Tomcat 的线程是复用的，漏清会把上一个请求的登录人带给下一个请求。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 取当前登录人，没有就抛 401 */
    public static CurrentUser require() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        return user;
    }

    public static Long requireUserId() {
        return require().id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
