package com.wy0225.imbrlabel.interceptor;

import com.wy0225.imbrlabel.constant.ErrorCode;
import com.wy0225.imbrlabel.constant.JwtClaimsConstant;
import com.wy0225.imbrlabel.context.BaseContext;
import com.wy0225.imbrlabel.exception.BusinessException;
import com.wy0225.imbrlabel.properties.JwtProperties;
import com.wy0225.imbrlabel.utils.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            // 获取当前登陆人id
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
            // 获取redis存储的令牌
            Boolean redisToken = redisTemplate.hasKey(token);
            // 令牌已失效（修改密码后删除token就会失效，无法访问）
            if (!redisToken) {
                response.setStatus(401); // 设置HTTP状态码为401，表示未授权。
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Invalid Token"); // 抛出业务异常。
            }
            //3、通过，放行
            return true;
        }  catch (ExpiredJwtException e) {
            // JWT已过期
            log.error("JWT已过期: {}", e.getMessage(), e);
            response.setStatus(401);
            return false;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            log.error("jwt令牌校验失败:{}", ex.getMessage());
            return false;
        }
    }

}
