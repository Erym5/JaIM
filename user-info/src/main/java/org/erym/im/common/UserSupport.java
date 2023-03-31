package org.erym.im.common;

import org.erym.im.common.exception.ConditionException;
import org.erym.im.common.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author cjt
 * @date 2022/6/7 0:12
 */
@Component
public class UserSupport {

    public Integer getCurrentUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String token = request.getHeader("token");
        Integer userId = Math.toIntExact(TokenUtil.verifyToken(token));
        if (userId < 0) {
            throw new ConditionException("非法用户");
        }
        return userId;
    }
}
