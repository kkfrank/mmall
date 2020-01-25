package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.ShippingService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/shippings")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @RequestMapping(value = "/{id}" ,method =  RequestMethod.GET)
    @ResponseBody
    public ServerResponse<Shipping> get(HttpServletRequest request, @PathVariable("id") Integer shippingId){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.get(user.getId(), shippingId);
    }

    @RequestMapping(value = "/search}" ,method =  RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpServletRequest request,
                                           @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.search(user.getId(), pageNum, pageSize);
    }


    @RequestMapping(value = "/" ,method =  RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> create(HttpServletRequest request, Shipping shipping){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.create(user.getId(), shipping);
    }


    @RequestMapping(value = "/{id}" ,method =  RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<String> delete(HttpServletRequest request, @PathVariable("id") Integer id){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.delete(user.getId(), id);
    }


    @RequestMapping(value = "/{id}" ,method =  RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<Shipping> update(HttpServletRequest request,  Shipping shipping){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.update(user.getId(), shipping);
    }
}
