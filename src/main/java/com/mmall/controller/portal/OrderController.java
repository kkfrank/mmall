package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.OrderService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse create(HttpServletRequest request, Integer shippingId){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.create(user.getId(), shippingId);
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse cancel(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.cancel(user.getId(), orderNo);
    }

    //获取购物车中已经选中的商品详情
    @RequestMapping(value = "/get_order_cart_product", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getOrderCartProduct(user.getId());
    }

    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse get(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.getByUserIdOrderNo(user.getId(), orderNo);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse search(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.listByUserId(user.getId(), pageNum, pageSize);
    }


    @RequestMapping("/pay")
    @ResponseBody
    public ServerResponse pay(Long orderNo, HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return orderService.pay(orderNo, user.getId());
    }

    @RequestMapping("/alipay_callback")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request) {
        Map<String , String > params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iterator = requestParams.keySet().iterator(); iterator.hasNext();){
            String name = (String)iterator.next();
            String[] values = (String [])requestParams.get(name);

            String str = "";

            for (int i = 0; i < values.length; i++) {
                str = (i == values.length -1) ? (str + values[i]) : (str + values[i] + ",");
            }
            params.put(name, str);

        }
        logger.info("支付宝回调,sign:{}, trade_status:{},参数:{}", params.get("sign"), params.get("trade_status"), params.toString());

         //验证回调的正确性，并避免重复通知

        params.remove("sign_type");
        try {
            boolean status = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8", Configs.getSignType());
            if(!status){
                return ServerResponse.createByErrorMsg("非法请求，验证不通过");
            }
        }catch (AlipayApiException e){
            logger.info("支付宝验证回调异常", e);
        }

        //todo 验证各种数据 放到service里

         ServerResponse serverResponse =  orderService.alipayCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallbackStatus.RESPONSE_SUCCESS.getValue();
        }
        return Const.AlipayCallbackStatus.RESPONSE_FAILED.getValue();
    }

    @RequestMapping("/query_order_pay_status")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpServletRequest request, Long orderNo){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        ServerResponse serverResponse = orderService.queryOrderPayStatus(user.getId(), orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }

}
