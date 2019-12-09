package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.FileService;
import com.mmall.service.ProductService;
import com.mmall.service.UserService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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
    public ServerResponse<ProductDetailVo> getProduct(HttpSession session, @PathVariable("id") Integer id){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return productService.getProduct(id, Const.ProductStatus.ON_SALE.getCode());
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session,
                                                  @RequestParam(value = "keyword", required = false) String keyword,
                                                  @RequestParam(value = "orderBy", defaultValue = "") String orderBy,
                                                  @RequestParam(value = "categoryId", required = false) Integer categoryId,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return productService.searchProductByKeyword(keyword, orderBy, categoryId, pageNum, pageSize);
    }
}
