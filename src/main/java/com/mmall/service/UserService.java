package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface UserService {
    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkUsername(String username);

    ServerResponse<String> checkEmail(String email);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew);

    ServerResponse<User> updateUserInfo(User user);

    ServerResponse<User> getUserInfo(Integer userId);

    ServerResponse checkAdminRole(User user);
}
