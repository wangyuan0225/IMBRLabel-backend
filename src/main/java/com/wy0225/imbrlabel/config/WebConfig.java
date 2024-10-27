package com.wy0225.imbrlabel.config;

import com.wy0225.imbrlabel.interceptor.JwtTokenAdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置静态资源路径
 * @author wangy
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadDir);
    }

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录和注册接口不拦截
        registry.addInterceptor(jwtTokenAdminInterceptor).excludePathPatterns("/user/login", "/user/register");
    }
}
