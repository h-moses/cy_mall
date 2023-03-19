package com.ms.order.config;

import com.ms.order.config.resolvers.TokenToAdminUserMethodArgumentResolver;
import com.ms.order.config.resolvers.TokenToMallUserMethodArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

@Configuration
public class OrderWebConfig implements WebMvcConfigurer {

    @Resource
    @Lazy
    private TokenToAdminUserMethodArgumentResolver tokenToAdminUserMethodArgumentResolver;

    @Resource
    @Lazy
    private TokenToMallUserMethodArgumentResolver tokenToMallUserMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(tokenToAdminUserMethodArgumentResolver);
        resolvers.add(tokenToMallUserMethodArgumentResolver);
    }
}
