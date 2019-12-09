package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.CategoryService;
import com.mmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/categories")
public class CategoryManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @RequestMapping(value="", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Category> addCategory(HttpSession session,
                       String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }

        return categoryService.addCategory(categoryName, parentId);
    }

    @RequestMapping(value="/{categoryId}/name", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse updateCategoryName(HttpSession session, @PathVariable("categoryId") Integer id,
                                             @RequestParam(value = "name") String categoryName){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }

        return categoryService.updateCategoryName(id, categoryName);
    }

    /**
     * 查询子节点category, 只查子节点
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value="/children", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getChildrenCategory(HttpSession session,
                              @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId,
                              @RequestParam(value = "deep", defaultValue = "false") boolean deep){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        if(!userService.checkAdminRole(user).isSuccess()){
            return ServerResponse.createByErrorMsg("用户没有权限");
        }
        if(Boolean.TRUE.equals(deep)){
            return categoryService.getChildrenCategoryDeep(categoryId);
        }else{
            return categoryService.getChildrenCategory(categoryId);

        }
    }
//
//    @RequestMapping(value="/get-category-deep", method = RequestMethod.GET)
//    @ResponseBody
//    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,
//                                              @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
//        }
//        if(!userService.checkAdminRole(user).isSuccess()){
//            return ServerResponse.createByErrorMsg("用户没有权限");
//        }
//    }
}
