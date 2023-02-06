package com.ms.product.config.resolvers;

import com.ms.common.annotation.TokenToAdminUser;
import com.ms.common.api.CommonResult;
import com.ms.common.enums.ServiceResultEnum;
import com.ms.common.exception.MallException;
import com.ms.product.config.entity.Admin;
import com.ms.product.entity.LoginAdminUser;
import com.ms.user.api.UserServiceFeign;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;

@Component
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
            if (null != token && !"".equals(token) && token.length() == 32) {
                CommonResult adminUserByToken = userServiceFeign.getAdminUserByToken(token);
                if (null == adminUserByToken || adminUserByToken.getCode() != 200 || adminUserByToken.getData() == null) {
                    MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
                }
                Admin data = (Admin) adminUserByToken.getData();

                LoginAdminUser loginAdminUser = new LoginAdminUser();
                loginAdminUser.setAdminUserId(data.getAdminUserId());
                loginAdminUser.setLoginUserName(data.getLoginUserName());
                loginAdminUser.setNickName(data.getNickName());
                loginAdminUser.setLocked(data.getLocked());
                return loginAdminUser;
            } else {
                MallException.fail(ServiceResultEnum.ADMIN_NOT_LOGIN_ERROR.getResult());
            }
        }
        return null;
    }
}
