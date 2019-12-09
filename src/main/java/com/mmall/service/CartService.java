package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface CartService {
    ServerResponse add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> delete(Integer userId, String productIds);
    ServerResponse<CartVo> search(Integer userId);
}
