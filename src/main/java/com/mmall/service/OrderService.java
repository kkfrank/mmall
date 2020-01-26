package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

import java.util.Map;

public interface OrderService {

//    ServerResponse<Order> get(Integer id);

    ServerResponse<OrderVo> getByUserIdOrderNo(Integer userId, Long orderNo);

    ServerResponse<PageInfo> listByUserId(Integer userId, int pageNum, int pageSize);

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> manageDetail(Long orderNo);

    ServerResponse pay(Long orderNo, Integer userId);

    ServerResponse alipayCallback(Map<String , String > params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);

    ServerResponse create(Integer userId, Integer shippingId);

    ServerResponse cancel(Integer userId, Long orderNo);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    ServerResponse manageSendGoods(Long orderNo);

    //hour个小时内未付款订单，关闭
    void closeOrder(int hour);
}
