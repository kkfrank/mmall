package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.CartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCart(HttpSession session, Integer productId, Integer count){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.add(user.getId(), productId, count);
    }


    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<CartVo> updateCart(HttpSession session, Integer productId, Integer count){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.update(user.getId(), productId, count);
    }


    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<CartVo> deleteCart(HttpSession session, String productIds){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.delete(user.getId(),productIds );
    }

    @RequestMapping(value = "/search", method = RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<CartVo> searchCart(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.search(user.getId());
    }
}
