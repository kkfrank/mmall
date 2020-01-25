package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.OrderService;
import com.mmall.service.UserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage/orders")
public class OrderManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpServletRequest request,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return orderService.manageList(pageNum, pageSize);
    }


    @RequestMapping(value = "detail", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<OrderVo> get(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return orderService.manageDetail(orderNo);
    }

    @RequestMapping(value = "search", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpServletRequest request,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return orderService.manageSearch(orderNo, pageNum, pageSize);
    }


    @RequestMapping(value = "send_goods", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<OrderVo> sendGoods(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return orderService.manageSendGoods(orderNo);
    }

}
