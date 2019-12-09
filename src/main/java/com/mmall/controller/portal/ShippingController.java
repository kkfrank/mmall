package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shippings")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;

    @RequestMapping(value = "/{id}" ,method =  RequestMethod.GET)
    @ResponseBody
    public ServerResponse<Shipping> get(HttpSession session, @PathVariable("id") Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.get(user.getId(), shippingId);
    }

    @RequestMapping(value = "/search}" ,method =  RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpSession session,
                                           @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.search(user.getId(), pageNum, pageSize);
    }


    @RequestMapping(value = "/" ,method =  RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Shipping> create(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.create(user.getId(), shipping);
    }


    @RequestMapping(value = "/{id}" ,method =  RequestMethod.DELETE)
    @ResponseBody
    public ServerResponse<String> delete(HttpSession session, @PathVariable("id") Integer id){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.delete(user.getId(), id);
    }


    @RequestMapping(value = "/{id}" ,method =  RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<Shipping> update(HttpSession session,  Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return  ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return shippingService.update(user.getId(), shipping);
    }
}
