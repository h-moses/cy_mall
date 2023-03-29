package com.ms.order.config.resolvers;

import com.ms.common.annotation.TokenToAdminUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.order.entity.LoginAdmin;
import com.ms.user.api.UserServiceFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

@Component
@Slf4j
public class TokenToAdminUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Resource
    private UserServiceFeign userServiceFeign;

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
            log.info("token: " + token);
            if (null != token && !"".equals(token) && token.length() == 32) {
                CommonResult adminUserByToken = userServiceFeign.getAdminUserByToken(token);
                if (null == adminUserByToken || adminUserByToken.getCode() != 200 || adminUserByToken.getData() == null) {
                    MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
                }
                LinkedHashMap data = (LinkedHashMap) adminUserByToken.getData();

                LoginAdmin loginAdminUser = new LoginAdmin();
                loginAdminUser.setAdminUserId(Long.valueOf(data.get("adminUserId").toString()));
                loginAdminUser.setLoginUserName((String) data.get("loginUserName"));
                loginAdminUser.setNickName((String) data.get("nickName"));
                loginAdminUser.setLocked(Byte.valueOf(data.get("locked").toString()));
                return loginAdminUser;
            } else {
                MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
