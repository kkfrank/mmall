package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

public interface ShippingService {
    ServerResponse<Shipping> get(Integer userId, Integer id);
    ServerResponse<Shipping> update(Integer userId, Shipping shipping);
    ServerResponse<String> delete(Integer userId, Integer shippingId);
    ServerResponse<Shipping> create(Integer userId, Shipping shipping);
    ServerResponse<PageInfo> search(Integer userId, int pageNum, int pageSize);
}
