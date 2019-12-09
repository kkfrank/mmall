package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.OrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service("OrderService")
public class OrderServiceImpl implements OrderService {

    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    @Override
    public ServerResponse create(Integer userId,  Integer shippingId) {
        if(userId == null || shippingId == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // todo check shppingId belong to this user

        List<Cart> cartList= cartMapper.selectCheckedCartByUserId(userId);

        //计算订单总价
        ServerResponse serverResponse = this.getCartOrderItems(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }

        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        BigDecimal totalAmount = new BigDecimal(0);
        for (OrderItem orderItem : orderItemList){
            totalAmount = BigDecimalUtil.add(totalAmount.doubleValue(),orderItem.getTotalPrice().doubleValue() );
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setShippingId(shippingId);
        order.setUserId(userId);
        order.setStatus(Const.OrderStatus.UN_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentType.ONLINE_PAY.getCode());
        order.setPayment(totalAmount);

//        int rowCount = orderMapper.insertSelective(order);
        int rowCount = orderMapper.insert(order);
        if(rowCount == 0){
            return ServerResponse.createBySuccessMsg("创建订单失败");
        }

        for (OrderItem orderItem: orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }

        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);
        //生成成功后，减少商品库存
        this.reduceProductStock(orderItemList);
        //清空购购物车 没checked的也clear?
        this.clearCartList(cartList);

        //返回订单明细
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }


    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单未找到");
        }
        if (order.getStatus() != Const.OrderStatus.UN_PAY.getCode()){
            return ServerResponse.createByErrorMsg("订单不是未支付状态，不能取消");
        }
        order.setStatus(Const.OrderStatus.CANCELED.getCode());

        // todo 取消订单，加库存？
//        Order updateOrder =new Order();
//        updateOrder.setId(order.getId());
//        updateOrder.setStatus(Const.OrderStatus.CANCELED.getCode());

        int rowCount = orderMapper.updateByPrimaryKeySelective(order);
        if (rowCount == 0){
            return ServerResponse.createByErrorMsg("取消订单失败");
        }
        return ServerResponse.createBySuccess();
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItems(userId, cartList);
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();

        List<OrderItemVo> orderItemVoList = new ArrayList<>();

        BigDecimal amount = new BigDecimal("0");
        for(OrderItem orderItem: orderItemList){
            amount = BigDecimalUtil.add(amount.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }

        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setProductTotalPrice(amount);

        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse<OrderVo> getByUserIdOrderNo(Integer userId, Long orderNo) {
       Order order =  orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
       if(order == null){
           return ServerResponse.createByErrorMsg("订单未找到");
       }
       List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);

       OrderVo orderVo = assembleOrderVo(order, orderItemList);
       return ServerResponse.createBySuccess(orderVo);
    }

//    @Override
//    public ServerResponse<Order> get(Integer id) {
//        if(id == null){
//            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
//        }
//        Order order = orderMapper.selectByPrimaryKey(id);
//        if(order == null){
//            return ServerResponse.createByErrorMsg("订单未找到");
//        }
//        return ServerResponse.createBySuccess(order);
//    }

    @Override
    public ServerResponse<PageInfo> listByUserId(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = new ArrayList<>();
        for(Order order: orderList){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(), userId);
            orderVoList.add(assembleOrderVo(order, orderItemList));
        }

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    //manage list
    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectAll();

        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList){
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());

            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse manageDetail(Long orderNo){
        if(orderNo == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }
        List<OrderItem> orderItemVoList = orderItemMapper.selectByOrderNo(orderNo);

        OrderVo orderVo = assembleOrderVo(order, orderItemVoList);
        return ServerResponse.createBySuccess(orderVo);
    }


    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize){
        if(orderNo == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }
        List<OrderItem> orderItemVoList = orderItemMapper.selectByOrderNo(orderNo);
        OrderVo orderVo = assembleOrderVo(order, orderItemVoList);

        PageHelper.startPage(pageNum, pageSize);
        PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
        pageInfo.setList(Lists.newArrayList(orderVo));
        return ServerResponse.createBySuccess(pageInfo);
    }


    public ServerResponse manageSendGoods(Long orderNo){
        if(orderNo == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }

        if(Const.OrderStatus.PAID.getCode() == order.getStatus()){// already paid
            order.setStatus(Const.OrderStatus.SHIPPED.getCode());
            order.setSendTime(new Date());
            int rowCount = orderMapper.updateByPrimaryKeySelective(order);
            if(rowCount == 1){
                return ServerResponse.createBySuccess("发货成功");
            }
            return ServerResponse.createBySuccess("设置失败，请重试");
        }
        return ServerResponse.createBySuccess("订单未付款，或者已发货");
    }


    @Override
    public ServerResponse pay(Long orderNo, Integer userId) {
        Map<String, String> reseultMap = new HashMap<>();
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }
        if(Const.OrderStatus.UN_PAY.getCode() != order.getStatus()){
            return ServerResponse.createByErrorMsg("订单状态不是待付款");
        }
        reseultMap.put("orderNo", String.valueOf(order.getOrderNo()));


        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = String.valueOf(order.getOrderNo());

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymall扫码支付，订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单号:").append(outTradeNo).append("共").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);

        for(OrderItem orderItem: orderItemList){
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        Configs.init("zfbinfo.properties");

        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);
                // 需要修改为运行机器上的路径
//                String filePath = String.format("/Users/sudo/Desktop/qr-%s.png",
//                        response.getOutTradeNo());
//                logger.info("filePath:" + filePath);
                //                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                reseultMap.put("qrUrl", response.getQrCode());
                return ServerResponse.createBySuccess(reseultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMsg("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMsg("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMsg("不支持的交易状态，交易返回异常!!!");
        }
    }

    public ServerResponse alipayCallback(Map<String , String > params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }
        if(order.getStatus() >= Const.OrderStatus.PAID.getCode()){
            return ServerResponse.createBySuccessMsg("支付宝重复调用");
        }
        if(Const.AlipayCallbackStatus.TRADE_SUCCESS.getValue().equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatus.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo){
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if(order == null){
            return ServerResponse.createByErrorMsg("订单不存在");
        }
        if(order.getStatus() >= Const.OrderStatus.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    private ServerResponse getCartOrderItems(Integer userId, List<Cart> cartList){
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMsg("购物车为空");
        }

        List<OrderItem> orderItemList = new ArrayList<>();
        for (Cart cart: cartList){
            OrderItem orderItem = new OrderItem();
            //校验产品状态和库存
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if(Const.ProductStatus.ON_SALE.getCode()!= product.getStatus()){
                return ServerResponse.createByErrorMsg("产品:"+ product.getName()+"已下架");
            }
            if(cart.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMsg("产品:"+ product.getName()+"库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(cart.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));

            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private void clearCartList(List<Cart> cartList){
        for(Cart cart: cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderId(order.getId());
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setUserId(order.getUserId());
        orderVo.setPayment(order.getPayment());

        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentType.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatus.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping!=null){
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for(OrderItem orderItem : orderItemList){
            orderItemVoList.add(this.assembleOrderItemVo(orderItem));
        }
        orderVo.setOrderItemList(orderItemVoList);

        return orderVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();

        shippingVo.setId(shipping.getId());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());

        shippingVo.setReceiverCity(shipping.getReceiverCity());

        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverZip(shipping.getReceiverZip());

        shippingVo.setCreateTime(shipping.getCreateTime());
        shipping.setUpdateTime(shipping.getUpdateTime());
        return shippingVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();

        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());

        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        orderItemVo.setUpdateTime(DateTimeUtil.dateToStr(orderItem.getUpdateTime()));

        return orderItemVo;
    }

    private long generateOrderNo(){
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }
}

