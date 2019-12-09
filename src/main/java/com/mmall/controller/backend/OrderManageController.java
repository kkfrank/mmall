package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.OrderService;
import com.mmall.service.UserService;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/orders")
public class OrderManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse<OrderVo> get(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse<PageInfo> search(HttpSession session,
                                          @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
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
    public ServerResponse<OrderVo> sendGoods(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return orderService.manageSendGoods(orderNo);
    }

}
