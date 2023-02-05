package com.ms.user.config.resolvers;

import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.user.config.annotations.TokenToAdminUser;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;

@Component
public class TokenToAdminUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private RedisTemplate redisTemplate;

    public TokenToAdminUserMethodArgumentResolver() {
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(TokenToAdminUser.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if (parameter.getParameterAnnotation(TokenToAdminUser.class) instanceof TokenToAdminUser) {
            String token = webRequest.getHeader("token");
            if (null != token && !"".equals(token) && token.length() == 32) {
                ValueOperations val = redisTemplate.opsForValue();
                Object adminToken = val.get(token);
                if (null == adminToken) {
                    MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
                }
                return adminToken;
            } else {
                MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
