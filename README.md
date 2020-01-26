oauth2.0协议（根据QQ登录综合）
1、根据APPID、APPKey、回调地址生成QQ登录授权链接，看电脑上有哪些QQ登录了可以使用，没有登录就账号密码登录QQ，state可以用UUID生成，就是用来防止CSRF攻击（模拟站点请求攻击）
2、用户选择账号后，重定向到回调地址，返回一个Authorization Code值
3、通过Authorization Code获取Access Token
4、通过Access Token 获取openID
5、通过openID获取用户的信息（openID是唯一的）

问题：腾讯官方有提供一个专门的开发jar包用于QQ联合登录，为什么还要用HTTPclient拼接参数进行QQ联合登陆？
     腾讯官方的jar虽然简化了QQ联合登录的参数拼接 ，但是通过源码查看发现，如果生成QQ登录授权链接的域名和回调地址的域名不一致的情况下，就会出现Access Token为空的情况，因为生成state防止CSRF攻击的时候，是把state放到session下的，如果请求授权链接的域名和回调地址的域名不一致，就会找不到state就会出现Access Token 为空