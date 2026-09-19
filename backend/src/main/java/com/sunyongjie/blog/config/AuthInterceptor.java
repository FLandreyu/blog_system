package com.sunyongjie.blog.config;

import com.sunyongjie.blog.common.BizException;
import com.sunyongjie.blog.common.CurrentUser;
import com.sunyongjie.blog.common.RequireAdmin;
import com.sunyongjie.blog.common.RequireLogin;
import com.sunyongjie.blog.common.ResultCode;
import com.sunyongjie.blog.common.UserContext;
import com.sunyongjie.blog.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

/**
 * 登录 / 角色校验拦截器。
 *
 * <p>规则由控制器上的 {@link RequireLogin} / {@link RequireAdmin} 注解声明，
 * 拦截器只负责统一执行 —— 这样「哪个接口要登录」在 Controller 上一眼可见，
 * 不用在配置里维护一长串路径匹配。
 *
 * <p>抛出的 {@link BizException} 会被 GlobalExceptionHandler 接住，
 * 所以 401/403 的响应体依然是统一的 Result 结构。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 静态资源等非 Controller 处理器直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 跨域预检请求不带 Authorization，放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        boolean needAdmin = hasAnnotation(handlerMethod, RequireAdmin.class);
        boolean needLogin = needAdmin || hasAnnotation(handlerMethod, RequireLogin.class);

        String token = resolveToken(request);
        if (token == null) {
            // 公开接口匿名可访问；需要登录的接口在这里拦下
            if (needLogin) {
                throw new BizException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
            }
            return true;
        }

        CurrentUser user;
        try {
            user = jwtUtil.parse(token);
        } catch (JwtException | IllegalArgumentException e) {
            // token 过期 / 被篡改 / 格式不对 —— 都是 401，不能让它们冒泡成 500
            if (needLogin) {
                throw new BizException(ResultCode.UNAUTHORIZED, "登录状态无效，请重新登录");
            }
            // 公开接口带了坏 token，按匿名处理即可
            return true;
        }

        UserContext.set(user);

        if (needAdmin && !user.isAdmin()) {
            throw new BizException(ResultCode.FORBIDDEN, "无权限访问该接口");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        // Tomcat 线程是复用的，必须清理，否则登录态会"串"到下个请求
        UserContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private boolean hasAnnotation(HandlerMethod handlerMethod, Class<? extends Annotation> type) {
        return handlerMethod.getMethodAnnotation(type) != null
                || handlerMethod.getBeanType().getAnnotation(type) != null;
    }
}
