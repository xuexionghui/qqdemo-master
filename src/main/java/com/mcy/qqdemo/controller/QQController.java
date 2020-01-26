package com.mcy.qqdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.mcy.qqdemo.custom.QQHttpClient;
import com.mcy.qqdemo.custom.QQStateErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

@Controller
public class QQController {

    

    /**
     * 发起请求
     * @param session
     * @return
     */
    @GetMapping("/qq/login")
    public String qq(HttpSession session) throws UnsupportedEncodingException {
        //QQ互联中的回调地址
    	String backUrl ="http://xuexionghui.natapp1.cc/qqLoginBack";
        //用于第三方应用防止CSRF攻击
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        session.setAttribute("state",uuid);

        //Step1：获取Authorization Code
        //生成QQ登录授权链接，看电脑上有哪些QQ登录了可以使用，没有登录就账号密码登录QQ
        String url = "https://graph.qq.com/oauth2.0/authorize?response_type=code"+
                "&client_id=" + QQHttpClient.APPID +
                "&redirect_uri=" + URLEncoder.encode(backUrl, "utf-8") +
                "&state=" + uuid;

        return "redirect:" + url;
    }

    /**
     * QQ回调
     * @param request    /qq/callback
     * @return
     */
    //Step2：用户选择账号后，使用重定向方式跳转回调地址，返回一个Authorization Code值
    //这里的访问路径要设置成QQ回调的地址
    @GetMapping("/qqLoginBack")
    public String qqcallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        //qq返回的信息
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String uuid = (String) session.getAttribute("state");

        if(uuid != null){
            if(!uuid.equals(state)){
                throw new QQStateErrorException("QQ,state错误");
            }
        }

        
        //Step3：通过Authorization Code获取Access Token
        String backUrl ="http://xuexionghui.natapp1.cc/qqLoginBack";
        String url = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code"+
                "&client_id=" + QQHttpClient.APPID +
                "&client_secret=" + QQHttpClient.APPKEY +
                "&code=" + code +
                "&redirect_uri=" + backUrl;

        String access_token = QQHttpClient.getAccessToken(url);

        //Step4: 通过Access Token 获取回调后的 openid 值
        url = "https://graph.qq.com/oauth2.0/me?access_token=" + access_token;
        String openid = QQHttpClient.getOpenID(url);

        //Step5：获取QQ用户信息
        url = "https://graph.qq.com/user/get_user_info?access_token=" + access_token +
                "&oauth_consumer_key="+ QQHttpClient.APPID +
                "&openid=" + openid;

        //返回用户的信息
        JSONObject jsonObject = QQHttpClient.getUserInfo(url);

        //也可以放到Redis和mysql中，只取出了部分数据，根据自己需要取
        session.setAttribute("openid",openid);  //openid,用来唯一标识qq用户
        session.setAttribute("nickname",(String)jsonObject.get("nickname")); //QQ名
        session.setAttribute("figureurl_qq_2",(String)jsonObject.get("figureurl_qq_2")); //大小为100*100像素的QQ头像URL

        return "redirect:/home";
    }
}
