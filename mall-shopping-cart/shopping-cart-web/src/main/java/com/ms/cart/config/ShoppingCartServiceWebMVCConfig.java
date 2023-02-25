package com.ms.cart.config;

import com.alibaba.cloud.seata.web.SeataHandlerInterceptor;
import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ms.cart.config.resolver.TokenToMallUserMethodArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Configuration
public class ShoppingCartServiceWebMVCConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ShoppingCartServiceWebMVCConfig.class);

    @Resource
    private SentinelProperties sentinelProperties;

    @Resource
    private Optional<SentinelWebInterceptor> sentinelWebInterceptor;

    @Resource
    private TokenToMallUserMethodArgumentResolver tokenToMallUserMethodArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(tokenToMallUserMethodArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SeataHandlerInterceptor()).addPathPatterns("/**");
        if (this.sentinelWebInterceptor.isPresent()) {
            SentinelProperties.Filter filter = this.sentinelProperties.getFilter();
            registry.addInterceptor(this.sentinelWebInterceptor.get()).order(filter.getOrder()).addPathPatterns(filter.getUrlPatterns());
            log.info("[Sentinel Starter] register SentinelWebInterceptor with urlPatterns: {}.", filter.getUrlPatterns());
        }
    }
}
