package com.mmall.common;

public class Const {
    public static final String CURRENT_USER = "CURRENT_USER";

    public static final int REDIS_SESSION_EXTIME = 60*30; //30 minutes
    public static final String TOKEN_PREFIX = "token_";

    public enum Role {
        USER(0,"USER1"),
        ADMIN(1,"ADMIN");

        private int id;
        private String role;
        Role(int id, String role) {
            this.id= id;
            this.role = role;
        }
        public int getId() {
            return id;
        }
    }

    public static void main(String [] arg){
        System.out.println(Role.values());
    }


    public enum ProductStatus {
        ON_SALE(1, "ON SALE");
        private int code;
        private String name;

        ProductStatus(int code,String name){
            this.name = name;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    public enum ProductListOrderBy{
        PRICE_ASC(0, "price_asc"),
        PRICE_DESC(1, "price_desc");

        private int value;
        private String name;

        ProductListOrderBy(int value, String name){
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum CartCheckStatus{
        UN_CHECKED(0, "UN_CHECKED"),
        CHECKED(1, "UN_CHECKED");
        private int value;
        private String name;
        CartCheckStatus(int value, String name){
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum CartLimitStatus{
        LIMIT_FAIL(0, "LIMIT_FAIL"),
        LIMIT_SUCCESSS(1, "LIMIT_SUCCESSS");
        private int value;
        private String name;

        CartLimitStatus(int value, String name){
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum OrderStatus{
        CANCELED(0, "已取消"),
        UN_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSED(60, "订单关闭");

        OrderStatus(int code, String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;

        public static OrderStatus codeOf(int code){
            for(OrderStatus orderStatus : values()){
                if(orderStatus.getCode() == code){
                    return orderStatus;
                }
            }
            throw new RuntimeException("未找到对应的订单状态");
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }

    public enum AlipayCallbackStatus{
        WAIT_BUYEr_PAY("WAIT_BUYEr_PAY"),
        TRADE_SUCCESS("TRADE_SUCCESS"),
        RESPONSE_SUCCESS("RESPONSE_SUCCESS"),
        RESPONSE_FAILED("RESPONSE_FAILED");

        private String value;
        AlipayCallbackStatus(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public enum PlatformEnum{
        ALIPAY(1, "支付宝");

        PlatformEnum(int code, String value){
            this.code = code;
            this.value = value;
        }
        private int code;
        private String value;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public enum PaymentType{
        ONLINE_PAY(1, "在线支付")
        ;
        PaymentType(int code, String value){
            this.code = code;
            this.value = value;
        }

        private int code;
        private String value;

        public static PaymentType codeOf(int code){
            for (PaymentType paymentType: values()){
                if(paymentType.getCode() == code){
                    return paymentType;
                }
            }
            throw new RuntimeException("未找到对应的支付方式");
//            return null;
        }
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

}
