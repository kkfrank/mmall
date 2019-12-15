package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/users")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
//    public ServerResponse<User> login(@RequestBody User user, HttpSession session){
    public ServerResponse<User> login (String username, String password, HttpSession session,
                                       HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse){
//        String username = user.getUsername();
//        String password = user.getPassword();
        ServerResponse<User> response = userService.login(username, password);
        if(response.isSuccess()){
//            session.setAttribute(Const.CURRENT_USER,response.getData());
            CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            RedisPoolUtil.setEx(session.getId(), Const.REDIS_SESSION_EXTIME, JsonUtil.obj2String(response.getData()));
        }
        return response;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
//        session.removeAttribute(Const.CURRENT_USER);

        CookieUtil.delLoginToken(httpServletRequest, httpServletResponse);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(@RequestBody User user, HttpSession session){
        return userService.register(user);
    }

    @RequestMapping(value = "/check-email", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkEmail(String email){
        return userService.checkEmail(email);
    }

    @RequestMapping(value = "/check-username", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkUsername(String username){
        return userService.checkUsername(username);
    }

    @RequestMapping(value = "/user-info", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
//        User user = (User)session.getAttribute(Const.CURRENT_USER);
        String userStr = RedisPoolUtil.get(session.getId());
        User user = JsonUtil.string2Obj(userStr, User.class);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        return ServerResponse.createBySuccess(user);
    }

    @RequestMapping(value = "/user-info", method = RequestMethod.PUT)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session, User request){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        request.setId(currentUser.getId());
        request.setUsername(currentUser.getUsername());
        ServerResponse<User> response = userService.updateUserInfo(request);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    @RequestMapping(value = "/forget-get-question", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return userService.selectQuestion(username);
    }

    @RequestMapping(value = "/forget-check-answer", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer){
        return userService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "/forget-reset-password", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        return userService.forgetResetPassword(username, passwordNew, forgetToken);
    }


    @RequestMapping(value = "/reset-password", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        return userService.resetPassword(user, passwordOld, passwordNew);
    }



}
