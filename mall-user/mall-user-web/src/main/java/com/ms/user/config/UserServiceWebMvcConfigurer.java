package com.ms.user.config;

import com.ms.user.config.resolvers.TokenToAdminUserMethodArgumentResolver;
import com.ms.user.config.resolvers.TokenToMallUserMethodArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class UserServiceWebMvcConfigurer implements WebMvcConfigurer {


    @Resource
    private TokenToAdminUserMethodArgumentResolver adminUserMethodArgumentResolver;

    @Resource
    private TokenToMallUserMethodArgumentResolver mallUserMethodArgumentResolver;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(adminUserMethodArgumentResolver);
        resolvers.add(mallUserMethodArgumentResolver);
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
    }
}