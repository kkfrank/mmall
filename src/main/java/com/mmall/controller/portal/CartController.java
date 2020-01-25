package com.mmall.controller.portal;

import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.CartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCart(HttpServletRequest request, Integer productId, Integer count){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.add(user.getId(), productId, count);
    }


    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<CartVo> updateCart(HttpServletRequest request, Integer productId, Integer count){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.update(user.getId(), productId, count);
    }


    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<CartVo> deleteCart(HttpServletRequest request, String productIds){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.delete(user.getId(),productIds );
    }

    @RequestMapping(value = "/search", method = RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<CartVo> searchCart(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.search(user.getId());
    }
}
