package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.FileService;
import com.mmall.service.ProductService;
import com.mmall.service.UserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/manage/products")
public class ProductManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Product> create(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return productService.saveOrUpdate(product);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<Product> update(HttpSession session, @PathVariable("id") Integer id, @RequestBody Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        product.setId(id);
        return productService.saveOrUpdate(product);
    }

    @RequestMapping(value = "/{id}/update-sale-status", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<String> updateSaleStatus(HttpSession session, @PathVariable("id") Integer id, Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return productService.updateSaleStatus(id, status);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> getProduct(HttpSession session, @PathVariable("id") Integer id){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return productService.getProduct(id, null);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<PageInfo> searchProduct(HttpSession session,
                                                      Integer productId,
                                                      String productName,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        return productService.searchProduct(productId, productName, pageNum, pageSize);
    }

    @RequestMapping(value = "/img-upload", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse imgUpload(HttpSession session, @RequestParam(value="upload_file", required = false) MultipartFile file, HttpServletRequest request){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
//        }
//        if(!userService.checkAdminRole(user).isSuccess()){
//            return ServerResponse.createByErrorMsg("用户没有权限");
//        }

        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = fileService.upload(file, path);
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

        Map<String, String> fileMap = new HashMap<>();
        fileMap.put("uri",targetFileName);
        fileMap.put("url",url);
        return ServerResponse.createBySuccess(fileMap);
    }

    @RequestMapping(value = "/rich-img-upload", method = RequestMethod.POST)
    @ResponseBody
    public Map richImgUpload(HttpSession session, @RequestParam(value="upload_file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = new HashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            resultMap.put("success",false);
//            resultMap.put("msg","请登录");
//            return resultMap;
//        }
//        if(!userService.checkAdminRole(user).isSuccess()){
//            resultMap.put("success",false);
//            resultMap.put("msg","没有权限");
//            return resultMap;
//        }

        String path = request.getSession().getServletContext().getRealPath("upload");
        String targetFileName = fileService.upload(file, path);
        if(StringUtils.isBlank(targetFileName)){
            resultMap.put("success",false);
            resultMap.put("msg","上传失败");
            return resultMap;
        }
        String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
        resultMap.put("success",true);
        resultMap.put("msg","上传成功");
        resultMap.put("file_path",url);
        response.addHeader("Access-Control-Allow_headers","X-File-name");
        return resultMap;
    }
}
























