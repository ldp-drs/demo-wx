package com.ldp.emos.wx.config.shiro;

import com.ldp.emos.wx.db.pojo.TbUser;
import com.ldp.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 用户认证与授权的Realm类
 */
@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权（验证权限时调用）
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        TbUser user = (TbUser) principalCollection.getPrimaryPrincipal();//获取用户信息
        int userId = user.getId();//获取用户id
        Set<String> permsSet = userService.searchUserPermissions(userId);//TODO 查询用户的权限列表

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();//授权对象
        info.setStringPermissions(permsSet);//TODO 把权限列表添加到info对象中
        return info;
    }

    /**
     * 认证（登录时使用）
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //TODO 从令牌中获取userId，然后检测该账户是否被冻结。
        String accessToken = (String) authenticationToken.getPrincipal();//获取token
        int userId = jwtUtil.getUserId(accessToken);//通过token获取用户id
        TbUser user = userService.searchById(userId);//通过用户id查询用户信息
        if (user == null) {
            throw new LockedAccountException("账号已锁定，请联系管理员");
        }
        //TODO 认证对象(用户信息，token，realm的名字)
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        //TODO 往info对象中添加用户信息、Token字符串
        return info;
    }
}
