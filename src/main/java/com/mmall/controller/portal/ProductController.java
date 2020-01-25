package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.FileService;
import com.mmall.service.ProductService;
import com.mmall.service.UserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisSharededPoolUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProduct(HttpServletRequest request, @PathVariable("id") Integer id){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return productService.getProduct(id, Const.ProductStatus.ON_SALE.getCode());
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpServletRequest request,
                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "orderBy", defaultValue = "") String orderBy,
                                                  @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisSharededPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return productService.searchProductByKeyword(keyword, orderBy, categoryId, pageNum, pageSize);
    }
}
