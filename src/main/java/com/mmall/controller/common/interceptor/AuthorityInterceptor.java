package com.mmall.controller.common.interceptor;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AuthorityInterceptor implements HandlerInterceptor{
    private static Logger logger = LoggerFactory.getLogger(AuthorityInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("preHandle");
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String methodName = handlerMethod.getMethod().getName();
        String className =handlerMethod.getBean().getClass().getSimpleName();

        //解析参数
        StringBuffer requestParamBuffer = new StringBuffer();
        Map paramMap = request.getParameterMap();

        //java8 lambada
        paramMap.forEach((key, value) -> {
            String itemValue = "";
            if(value instanceof String[]){
                itemValue = Arrays.toString((String [])value);
            }
            requestParamBuffer.append(key).append("=").append(itemValue);
        });

//        Iterator it = paramMap.entrySet().iterator();
//        while (it.hasNext()){
//           Map.Entry entry = (Map.Entry) it.next();
//           String key = (String)entry.getKey();
//           String[] value = (String [])entry.getValue();
//           String valStr = Arrays.toString(value);
//           requestParamBuffer.append(key).append("=").append(valStr);
//        }

        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if(!StringUtils.isEmpty(loginToken)){
            String userJsonStr = RedisSharededPoolUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }

        if(user == null || user.getRole() != Const.Role.ADMIN.getId()){
            response.reset();// 不调用会报 getWrite() has already been called for this response.
            response.setCharacterEncoding("UTF-8");//不设置会乱码
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            if(user == null){
                if(StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richImgUpload")){
                    logger.info("className:{}, methodName:{}", className, methodName);
                    Map resultMap = new HashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "拦截器拦截, 用户未登录");
                    out.print(JsonUtil.obj2String(resultMap));
                }else{
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMsg("拦截器拦截, 用户未登录")));
                }
            }else{
                if(StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richImgUpload")){
                    logger.info("className:{}, methodName:{}", className, methodName);
                    Map resultMap = new HashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "拦截器拦截,用户无权限");
                    out.print(JsonUtil.obj2String(resultMap));
                }else{
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMsg("拦截器拦截,用户无权限")));
                }

            }
            out.flush();
            out.close();
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        logger.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        logger.info("afterCompletion");
    }
}
