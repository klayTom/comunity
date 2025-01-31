package com.klay.controller;

import com.klay.dto.AccessTokenDto;
import com.klay.dto.GiteeUser;
import com.klay.model.User;
import com.klay.provider.GiteeProvider;
import com.klay.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
@Slf4j
public class AuthorizeController {
    @Autowired
    private GiteeProvider giteeProvider;

    @Value("${github.client.id}")
    private String clientId;

    @Value(("${github.client.secret}"))
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    @Autowired
    UserService userService;

    @GetMapping("/callback")
    public String callBack (@RequestParam(name = "code") String code, @RequestParam(name = "state") String state, HttpServletResponse response) {

        AccessTokenDto accessTokenDto = new AccessTokenDto();
        accessTokenDto.setClient_id(clientId);
        accessTokenDto.setClient_secret(clientSecret);
        accessTokenDto.setRedirect_uri(redirectUri);
        accessTokenDto.setCode(code);
        accessTokenDto.setState(state);
        String accessToken = giteeProvider.getAccessToken(accessTokenDto);
        //System.out.println(accessToken);
        GiteeUser giteeUser = giteeProvider.getUser(accessToken);
        //System.out.println(giteeUser.getAvatarUrl());

        if (giteeUser != null && giteeUser.getId()!=null) {
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(giteeUser.getName());
            user.setAccountId(String.valueOf(giteeUser.getId()));
            user.setAvatarUrl(giteeUser.getAvatarUrl());

            userService.createOrUpdate(user);
            // 登陆成功 将用户保存到 session 域中
            Cookie cookie = new Cookie("token", token);
            /*cookie.setMaxAge(60 * 60 * 1);*/
            response.addCookie(cookie);
            return "redirect:/";
        }else {
            // 登陆失败 ， 重新登录
            log.error("callback get github error {}", giteeUser);
            return "redirect:/";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpServletResponse response, HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        Cookie cookie = new Cookie("token", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/";
    }
}
