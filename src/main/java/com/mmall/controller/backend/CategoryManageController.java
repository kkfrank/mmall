package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.CategoryService;
import com.mmall.service.UserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public ServerResponse<Category> addCategory(HttpServletRequest request,
                       String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

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
    public ServerResponse updateCategoryName(HttpServletRequest request, @PathVariable("categoryId") Integer id,
                                             @RequestParam(value = "name") String categoryName){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

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
    public ServerResponse getChildrenCategory(HttpServletRequest request,
                              @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId,
                              @RequestParam(value = "deep", defaultValue = "false") boolean deep){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        String userJsonStr = RedisPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

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
