package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("shippingService")
public class ShippingServiceImpl implements ShippingService{

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Shipping> get(Integer userId, Integer shippingId) {
        Shipping shipping = shippingMapper.getByShippingIdUserId(shippingId, userId);
        if(shipping == null){
            return ServerResponse.createByErrorMsg("未找到");
        }
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse<Shipping> update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("更新失败");
        }
        return ServerResponse.createBySuccessMsg("更新成功");
    }

    @Override
    public ServerResponse<String> delete(Integer userId, Integer shippingId) {
//        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
//        if(shipping == null){
//            return ServerResponse.createByErrorMsg("地址不存在");
//        }
//        if(!userId.equals(shipping.getUserId())){
//            return ServerResponse.createByErrorMsg("地址不是此人的");
//        }
        int rowCount = shippingMapper.deleteByShippingIdUserId(shippingId, userId);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("删除失败");
        }
        return ServerResponse.createBySuccessMsg("删除成功");
    }

    @Override
    public ServerResponse<Shipping> create(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insertSelective(shipping);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("新增失败");
        }
        return ServerResponse.createBySuccess(shipping);
    }

    @Override
    public ServerResponse<PageInfo> search(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
