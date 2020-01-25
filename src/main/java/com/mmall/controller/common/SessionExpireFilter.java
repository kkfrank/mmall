package com.mmall.controller.common;

import com.mmall.common.Const;
import com.mmall.util.CookieUtil;
import com.mmall.util.RedisSharededPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SessionExpireFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String loginToken = CookieUtil.readLoginToken((HttpServletRequest) servletRequest);
        if(!StringUtils.isEmpty(loginToken)){
            String userJsonStr = RedisSharededPoolUtil.get(loginToken);
            if(!StringUtils.isEmpty(userJsonStr)){
                RedisSharededPoolUtil.expire(loginToken, Const.REDIS_SESSION_EXTIME);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
