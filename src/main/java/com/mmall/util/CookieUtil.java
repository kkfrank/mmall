package com.mmall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private final static String COOKIE_DOMAIN = ".fkk.com";
    private final static String COOKIE_NAME = "mmall_login_token";

    public static void writeLoginToken(HttpServletResponse response, String token){
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setDomain(COOKIE_DOMAIN);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        // 不不设置的话，cookie不会写入硬盘，而是在内存，只在当前页面有效
        // -1 永久有效
        cookie.setMaxAge(60*60*24*365);
        response.addCookie(cookie);
        logger.info("write cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
    }

    public static String readLoginToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie: cookies) {
                if(COOKIE_NAME.equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie: cookies){
                if(COOKIE_NAME.equals(cookie.getName())){
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    logger.info("del cookieName:{}, cookieValue:{}", cookie.getName(), cookie.getValue());
                    return;
                }
            }
        }
    }
}
