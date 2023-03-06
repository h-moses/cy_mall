package com.ms.mall.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ms.common.api.CommonResult;
import com.ms.common.pojo.UserToken;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final List<String> ignoreUrls =new ArrayList<>();
        ignoreUrls.add("/user/mall/login");
        ignoreUrls.add("/user/mall/register");
        ignoreUrls.add("/categories/mall/listAll");

//        登陆、注册、商品分类接口直接放行
        if (ignoreUrls.contains(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = exchange.getRequest().getHeaders();

        if (null == headers || headers.isEmpty()) {
            return wrapErrorResponse(exchange);
        }

        String token = headers.getFirst("token");
        if (!StringUtils.hasText(token)) {
            return wrapErrorResponse(exchange);
        }

        ValueOperations<String, UserToken> ops = redisTemplate.opsForValue();
        UserToken userToken = ops.get(token);
        if (null == userToken) {
            return wrapErrorResponse(exchange);
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    Mono<Void> wrapErrorResponse(ServerWebExchange exchange) {
        CommonResult<Object> result = CommonResult.failure("无权限访问");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode resultNode = mapper.valueToTree(result);
        byte[] bytes = resultNode.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(bytes);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().writeWith(Flux.just(dataBuffer));
    }
}
