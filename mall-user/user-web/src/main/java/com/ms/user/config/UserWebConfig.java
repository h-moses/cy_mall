package com.ms.user.config;

import com.ms.user.config.resolvers.TokenToAdminUserMethodArgumentResolver;
import com.ms.user.config.resolvers.TokenToMallUserMethodArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class UserWebConfig implements WebMvcConfigurer {

    @Resource
    private TokenToAdminUserMethodArgumentResolver adminUserMethodArgumentResolver;

    @Resource
    private TokenToMallUserMethodArgumentResolver mallUserMethodArgumentResolver;


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(adminUserMethodArgumentResolver);
        resolvers.add(mallUserMethodArgumentResolver);
    }
}
