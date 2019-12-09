package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);

        if (user == null) {
            return ServerResponse.createByErrorMsg("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
//        int resultCount = userMapper.checkUsername(user.getUsername());
//        if(resultCount > 0){
//            return ServerResponse.createByErrorMsg("用户名存在");
//        }
//        resultCount = userMapper.checkEmail(user.getEmail());
//        if(resultCount > 0){
//            return ServerResponse.createByErrorMsg("邮箱存在");
//        }
        ServerResponse response = this.checkUsername(user.getUsername());
        if(!response.isSuccess()){
            return response;
        }

        response = this.checkEmail(user.getEmail());
        if(!response.isSuccess()){
            return response;
        }

        user.setRole(Const.Role.USER.getId());
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("注册失败");
        }
        return ServerResponse.createBySuccessMsg("注册成功");
    }

    @Override
    public ServerResponse<String> checkEmail(String email) {
        int resultCount = userMapper.checkEmail(email);
        if(resultCount > 0){
            return ServerResponse.createByErrorMsg("邮箱存在");
        }
        return ServerResponse.createBySuccessMsg("邮箱不存在");
    }

    @Override
    public ServerResponse<String> checkUsername(String username) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount > 0){
            return ServerResponse.createByErrorMsg("用户名存在");
        }
        return ServerResponse.createBySuccessMsg("用户名不存在");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse response =  checkUsername(username);
        if(response.isSuccess()){//用户名不存在
            return ServerResponse.createByErrorMsg("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNoneBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMsg("找回密码问题为空");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount == 0){
            return ServerResponse.createByErrorMsg("答案错误");
        }

        String forgetToken = UUID.randomUUID().toString();
        TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
        return ServerResponse.createBySuccess(forgetToken);
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMsg("参数错误，token为空");
        }

        ServerResponse response =  checkUsername(username);
        if(response.isSuccess()){//用户名不存在
            return ServerResponse.createByErrorMsg("用户名不存在");
        }

        String token = TokenCache.getValue(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMsg("token无效或者过期");
        }
        if(!StringUtils.equals(token, forgetToken)){
            return ServerResponse.createByErrorMsg("token错误，请重新获取重置密码的token");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
        int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
        if(rowCount == 0){
            return ServerResponse.createBySuccessMsg("重置密码失败");
        }
        return ServerResponse.createBySuccessMsg("重置密码成功");
    }

    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        String md5PasswordOld = MD5Util.MD5EncodeUtf8(passwordOld);
        if(userMapper.checkPassword(user.getId(), md5PasswordOld) == 0){
            return ServerResponse.createByErrorMsg("旧密码错误");
        }

        String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
        int rowCount = userMapper.updatePasswordById(user.getId(), md5PasswordNew);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("重置密码失败");
        }
        return ServerResponse.createBySuccessMsg("重置密码成功");
    }

    @Override
    public ServerResponse<User> getUserInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMsg("用户不存在");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        // username不能更新
        // email如果存在，必须是当前的email
        int rowCount = userMapper.checkEmailByNotUserId(user.getId(), user.getEmail());
        if(rowCount > 0){
            return ServerResponse.createByErrorMsg("email存在");
        }
        user.setPassword(StringUtils.EMPTY);

        User updateUser = new User();
        updateUser.setId(user.getId());// ?
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setPhone(user.getPassword());

        rowCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(rowCount == 0){
            return ServerResponse.createByErrorMsg("更新失败");
        }
        return ServerResponse.createBySuccess("更新成功",updateUser);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user.getRole() == Const.Role.ADMIN.getId()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
