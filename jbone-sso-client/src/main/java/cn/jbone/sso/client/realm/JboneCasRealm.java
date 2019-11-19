package cn.jbone.sso.client.realm;

import cn.jbone.common.exception.JboneException;
import cn.jbone.sso.common.SsoConstants;
import cn.jbone.sso.common.domain.UserInfo;
import com.alibaba.fastjson.JSON;
import io.buji.pac4j.realm.Pac4jRealm;
import io.buji.pac4j.subject.Pac4jPrincipal;
import io.buji.pac4j.token.Pac4jToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.CollectionUtils;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JboneCasRealm extends Pac4jRealm {
    private static final Logger logger = LoggerFactory.getLogger(JboneCasRealm.class);

    private String serverName;

    public JboneCasRealm(EhCacheManager ehCacheManager, String serverName){
        this.setCacheManager(ehCacheManager);
        this.serverName = serverName;
    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        Pac4jToken pac4jToken = (Pac4jToken)token;
        List<CommonProfile> profiles = pac4jToken.getProfiles();
        Pac4jPrincipal principal = new Pac4jPrincipal(profiles, this.getPrincipalNameAttribute());

        String  username = principal.getName();
        //如果是第三方登录，通过三方名字_id查询
        CasProfile casProfile =(CasProfile) profiles.get(0);
        if(casProfile != null){
            Object clientName = casProfile.getAttribute("clientName");
            if(clientName != null){
                username = clientName.toString().toUpperCase() + "_" + casProfile.getId();
            }
        }

        /**
         * 将用户对象保存为身份信息，用于系统获取用户信息
         */
        UserInfo userInfo = JSON.parseObject(casProfile.getAttribute(SsoConstants.USER_INFO).toString(),UserInfo.class);

        if(userInfo == null ){
            throw new JboneException(String.format("user[%s] server[%s] is not found.", username, serverName));
        }

        //设置当前系统菜单
        if(userInfo.getAuthInfo() != null && !CollectionUtils.isEmpty(userInfo.getAuthInfo().getAllMenus())){
            userInfo.getAuthInfo().setMenus(userInfo.getAuthInfo().getAllMenus().get(serverName));
        }


        List<Object> principals = CollectionUtils.asList(new Object[]{userInfo, principal});
        SimplePrincipalCollection principalCollection = new SimplePrincipalCollection(principals, this.getName());

        return new SimpleAuthenticationInfo(principalCollection, pac4jToken.getCredentials());
    }

    /**
     * 权限认证，为当前登录的Subject授予角色和权限
     *  ：本例中该方法的调用时机为需授权资源被访问时
     *  ：并且每次访问需授权资源时都会执行该方法中的逻辑，这表明本例中默认并未启用AuthorizationCache
     *  ：如果连续访问同一个URL（比如刷新），该方法不会被重复调用，Shiro有一个时间间隔（也就是cache时间，在ehcache-shiro.xml中配置），超过这个时间间隔再刷新页面，该方法会被执行
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        logger.info("--加载Shiro权限认证--");
        UserInfo userInfo = principals.oneByType(UserInfo.class);
        Set<String> roles = userInfo.getAuthInfo().getRoles();
        Set<String> permissions = userInfo.getAuthInfo().getPermissions();

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        if(roles != null && !roles.isEmpty()){
            Iterator<String> iterator = roles.iterator();
            while (iterator.hasNext()){
                info.addRole(iterator.next());
            }
        }

        if(permissions != null && !permissions.isEmpty()){
            Iterator<String> iterator = permissions.iterator();
            while (iterator.hasNext()){
                info.addStringPermission(iterator.next());
            }
        }

        return info;
    }
}
