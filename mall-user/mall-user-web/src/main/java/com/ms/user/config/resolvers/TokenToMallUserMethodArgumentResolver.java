package com.ms.user.config.resolvers;

import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.common.pojo.UserToken;
import com.ms.user.config.annotations.TokenToMallUser;
import com.ms.user.entity.User;
import com.ms.user.mapper.UserMapper;
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
public class TokenToMallUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        if (parameter.hasParameterAnnotation(TokenToMallUser.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if (parameter.getParameterAnnotation(TokenToMallUser.class) instanceof TokenToMallUser) {
            String token = webRequest.getHeader("token");
            if (null != token && !"".equals(token) && token.length() == 32) {
                ValueOperations<String, UserToken> valueOperations = redisTemplate.opsForValue();
                UserToken userToken = valueOperations.get(token);
                if (null == userToken) {
                    MallException.fail(ServiceResultEnum.TOKEN_EXPIRE_ERROR.getResult());
                }
                User user = userMapper.selectById(userToken.getUserId());
                if (null == user) {
                    MallException.fail(ServiceResultEnum.USER_NULL_ERROR.getResult());
                }
                if (1 == user.getLockedFlag().intValue()) {
                    MallException.fail(ServiceResultEnum.LOGIN_USER_LOCKED_ERROR.getResult());
                }
                return userToken;
            } else {
                MallException.fail(ServiceResultEnum.NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
