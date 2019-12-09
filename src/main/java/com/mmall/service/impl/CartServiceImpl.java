package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.CartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("cartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        if(userId == null || productId == null || count == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        int rowCount = 0 ;

        if(cart == null){// create
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(count);
            cart.setChecked(Const.CartCheckStatus.CHECKED.getValue());
            rowCount = cartMapper.insert(cart);
        }else{// update count
            cart.setQuantity(cart.getQuantity() + count);
            rowCount = cartMapper.updateByPrimaryKeySelective(cart);
        }
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("添加购物车失败");
        }
        CartVo cartVo = getCartVolimit(userId);
//        Cart insertCart = cartMapper.selectByPrimaryKey(cart.getId());
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if(userId == null || productId == null || count == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdAndProductId(userId, productId);
        if(cart == null){// create
            return ServerResponse.createByErrorMsg("购物车不存在");
        }

        cart.setQuantity(count);
        int rowCount = cartMapper.updateByPrimaryKeySelective(cart); // why
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("添加购物车失败");
        }
        CartVo cartVo = getCartVolimit(userId);
//        Cart insertCart = cartMapper.selectByPrimaryKey(cart.getId());
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        List<String> arrayList = Arrays.asList(productIds.split(","));
        List<Integer>  productIdList = new ArrayList<>();
        for (String id : arrayList){
            productIdList.add(Integer.valueOf(id));
        }
        cartMapper.deleteByUserIdAndProductIds(userId, productIdList);
        CartVo cartVo = this.getCartVolimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }


    @Override
    public ServerResponse<CartVo> search(Integer userId) {
        CartVo cartVo = getCartVolimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    private CartVo getCartVolimit(Integer userId){
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if(CollectionUtils.isNotEmpty(cartList)){
            BigDecimal cartTotalPrice = new BigDecimal("0");
            Boolean allChecked = true;
            for(Cart cart : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setCartId(cart.getId());
                cartProductVo.setUserId(cart.getUserId());
                cartProductVo.setProductId(cart.getProductId());
//                cartProductVo.setQuantity(cart.getQuantity());

                Product product = productMapper.selectByPrimaryKey(cart.getProductId());

                if(product != null){
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
                    if(product.getStock() >= cart.getQuantity()){//stock is enough
                        cartProductVo.setQuantity(cart.getQuantity());
                        cartProductVo.setLimitQuantity(Const.CartLimitStatus.LIMIT_SUCCESSS.getName());
                    }else{
                        cartProductVo.setQuantity(product.getStock());
                        cartProductVo.setLimitQuantity(Const.CartLimitStatus.LIMIT_FAIL.getName());
                    }
                    //更新购物车中有效库存 why?
                    Cart cartForQuantity = new Cart();
                    cartForQuantity.setId(cart.getId());
                    cartForQuantity.setQuantity(cartProductVo.getQuantity());
                    cartMapper.updateByPrimaryKeySelective(cartForQuantity);

                    //计算此产品的总价
                    BigDecimal productTotalPrice = BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity());
                    cartProductVo.setProductTotalPrice(productTotalPrice);

                    cartProductVo.setProductChecked(cart.getChecked());
                    if(Const.CartCheckStatus.CHECKED.getValue() == cart.getChecked()){
                        cartTotalPrice = cartTotalPrice.add(productTotalPrice);
                    }else{
                        allChecked = false;
                    }
                    cartProductVoList.add(cartProductVo);
                }
            }
            cartVo.setCartProductVoList(cartProductVoList);
            cartVo.setAllChecked(allChecked);
            cartVo.setCartTotalPrice(cartTotalPrice);
            cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        }
        return cartVo;
    }
}
