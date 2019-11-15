package cn.jbone.sso.server.authentication;

import cn.jbone.common.rpc.Result;
import cn.jbone.common.utils.PasswordUtils;
import cn.jbone.sso.common.SsoConstants;
import cn.jbone.system.api.UserApi;
import cn.jbone.system.common.UserRequestDO;
import cn.jbone.system.common.UserResponseDO;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

public class JboneAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private Logger logger = LoggerFactory.getLogger(getName());

    private final String requiredRole;
    private UserApi userApi;

    public JboneAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order,final String requiredRole) {
        super(name, servicesManager, principalFactory, order);
        this.requiredRole = requiredRole;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword) throws GeneralSecurityException, PreventedException {
        UserRequestDO userRequestDO = UserRequestDO.buildAll(credential.getUsername(),null);
        Result<UserResponseDO> result = getUserApi().commonRequest(userRequestDO);

        if(result == null || !result.isSuccess() || result.getData() == null){
            logger.warn("用户[{}]不存在",credential.getUsername());
            throw new FailedLoginException("用户不存在");
        }

        UserResponseDO userResponseDO = result.getData();


        String caculatePwd = PasswordUtils.getMd5PasswordWithSalt(originalPassword,userResponseDO.getSecurityInfo().getSalt());
        if(!caculatePwd.equals(userResponseDO.getSecurityInfo().getPassword())){
            throw new FailedLoginException("密码错误");
        }


        if(userResponseDO.getSecurityInfo().getLocked() == 1){
            logger.warn("用户[{}]已被锁定",credential.getUsername());
            throw new FailedLoginException("用户已被锁定，禁止登录");
        }

        if(userResponseDO.getAuthInfo() == null || CollectionUtils.isEmpty(userResponseDO.getAuthInfo().getRoles()) || !userResponseDO.getAuthInfo().getRoles().contains(requiredRole)){
            logger.warn("用户[{}]没有登录权限",credential.getUsername());
            throw new FailedLoginException("用户没有登录权限");
        }
        Map<String,Object> attributes = new HashMap<>();
        attributes.put(SsoConstants.USER_INFO,userResponseDO);

        return createHandlerResult(credential,
                this.principalFactory.createPrincipal(credential.getUsername(),attributes));


    }



    public UserApi getUserApi() {
        return userApi;
    }

    public void setUserApi(UserApi userApi) {
        this.userApi = userApi;
    }
}
