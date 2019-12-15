package com.mmall.util;

import com.mmall.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认转换date成timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //统一所有的日期格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));


        //反序列化配置
        //忽略在json字符串存在，但在java对象中不存在属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2String(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Parse object to String error", e);
            return null;
        }
    }


    public static <T> String obj2StringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj :objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Parse Object to String error", e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str, clazz);
        } catch (Exception e) {
            logger.warn("Parse String to Object error", e);
        }
        return null;
    }


    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if(StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return typeReference.getType().equals(String.class) ? (T)str : objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            logger.warn("Parse String to Object error", e);
        }
        return null;
    }


    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementsClass){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementsClass);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (Exception e) {
            logger.warn("Parse String to Object error", e);
        }
        return null;
    }

    public static void main(String[] args) {
        User user1 = new User();
        User user2 = new User();
        user1.setId(1);
        user1.setEmail("sdf@qq.com");
        user2.setId(2);
        user2.setEmail("sdf@qq.com");

        String u1JSon = JsonUtil.obj2String(user1);
        String fak = JsonUtil.obj2String("sdfsdfsdf");
        System.out.println(u1JSon);
        System.out.println(JsonUtil.obj2StringPretty(user1));

        User user = JsonUtil.string2Obj(u1JSon, User.class);
        System.out.println(user);

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        String listStr = JsonUtil.obj2StringPretty(userList);
        System.out.println("-----------------");
        System.out.println(listStr);

        List<User> userList2 = JsonUtil.string2Obj(listStr, List.class);
        List<User> userList3 = JsonUtil.string2Obj(listStr, new TypeReference<List<User>>() {
        });

        List<User> userList4 = JsonUtil.string2Obj(listStr,List.class, User.class);
        System.out.println(userList2);
    }
}
