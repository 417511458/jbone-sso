package cn.jbone.sso.support.authentication.custom;

import cn.jbone.common.rpc.Result;
import cn.jbone.common.utils.PasswordUtils;
import cn.jbone.sso.common.SsoConstants;
import cn.jbone.sso.common.domain.*;
import cn.jbone.sso.common.exceptions.AccountInvalidPasswordException;
import cn.jbone.sso.common.exceptions.AccountPermissionDeniedException;
import cn.jbone.system.api.UserApi;
import cn.jbone.system.common.UserRequestDO;
import cn.jbone.system.common.UserResponseDO;
import cn.jbone.system.common.dto.response.MenuInfoResponseDTO;
import com.alibaba.fastjson.JSON;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            throw new AccountNotFoundException("用户不存在");
        }

        UserResponseDO userResponseDO = result.getData();


        String caculatePwd = PasswordUtils.getMd5PasswordWithSalt(originalPassword,userResponseDO.getSecurityInfo().getSalt());
        if(!caculatePwd.equals(userResponseDO.getSecurityInfo().getPassword())){
            throw new AccountInvalidPasswordException("密码错误");
        }


        if(userResponseDO.getSecurityInfo().getLocked() == 1){
            logger.warn("用户[{}]已被锁定",credential.getUsername());
            throw new AccountLockedException("用户已被锁定，禁止登录");
        }

        if(userResponseDO.getAuthInfo() == null || CollectionUtils.isEmpty(userResponseDO.getAuthInfo().getRoles()) || !userResponseDO.getAuthInfo().getRoles().contains(requiredRole)){
            logger.warn("用户[{}]没有登录权限",credential.getUsername());
            throw new AccountPermissionDeniedException("用户没有登录权限");
        }
        Map<String,Object> attributes = new HashMap<>();
        UserInfo userInfo = converUserInfo(userResponseDO);
        attributes.put(SsoConstants.USER_INFO, JSON.toJSONString(userInfo));
        attributes.put(SsoConstants.ROLES,getUserRoles(userInfo));
        attributes.put(SsoConstants.PERMISSIOINS,getUserPermissions(userInfo));
//          //注意，这里只能传基本类型，保存后从客户端获取
//        List<String> testList = new ArrayList<>();
//        testList.add("111");
//        testList.add("222");
//        attributes.put("testList",testList);
//        attributes.put("testUserInfo",converUserInfo(userResponseDO));
        return createHandlerResult(credential,
                this.principalFactory.createPrincipal(credential.getUsername(),attributes));


    }

    public List<String> getUserRoles(UserInfo userInfo){
        if(userInfo == null || userInfo.getAuthInfo() == null || CollectionUtils.isEmpty(userInfo.getAuthInfo().getRoles())){
            return null;
        }
        List<String> roles = new ArrayList<>();
        roles.addAll(userInfo.getAuthInfo().getRoles());
        return roles;
    }

    public List<String> getUserPermissions(UserInfo userInfo){
        if(userInfo == null || userInfo.getAuthInfo() == null || CollectionUtils.isEmpty(userInfo.getAuthInfo().getPermissions())){
            return null;
        }
        List<String> roles = new ArrayList<>();
        roles.addAll(userInfo.getAuthInfo().getPermissions());
        return roles;
    }

    private UserInfo converUserInfo(UserResponseDO userResponseDO){
        if(userResponseDO == null){
            return null;
        }
        UserInfo userInfo = new UserInfo();

        //用户基本信息
        UserBaseInfo baseInfo = new UserBaseInfo();
        baseInfo.setAvatar(userResponseDO.getBaseInfo().getAvatar());
        baseInfo.setEmail(userResponseDO.getBaseInfo().getEmail());
        baseInfo.setId(userResponseDO.getBaseInfo().getId());
        baseInfo.setPhone(userResponseDO.getBaseInfo().getPhone());
        baseInfo.setRealname(userResponseDO.getBaseInfo().getRealname());
        baseInfo.setSex(userResponseDO.getBaseInfo().getSex());
        baseInfo.setUsername(userResponseDO.getBaseInfo().getUsername());

        userInfo.setBaseInfo(baseInfo);


        //用户权限信息
        if(userResponseDO.getAuthInfo() != null){
            UserAuthInfo authInfo = new UserAuthInfo();
            authInfo.setPermissions(userResponseDO.getAuthInfo().getPermissions());
            authInfo.setRoles(userResponseDO.getAuthInfo().getRoles());
            authInfo.setAllMenus(convertMenuInfos(userResponseDO.getAuthInfo().getMenus()));

            userInfo.setAuthInfo(authInfo);
        }


        //用户安全信息
        if(userResponseDO.getSecurityInfo() != null){
            UserSecurityInfo userSecurityInfo = new UserSecurityInfo();
            userSecurityInfo.setLocked(userResponseDO.getSecurityInfo().getLocked());
            userSecurityInfo.setPassword(userResponseDO.getSecurityInfo().getPassword());
            userSecurityInfo.setSalt(userResponseDO.getSecurityInfo().getSalt());

            userInfo.setSecurityInfo(userSecurityInfo);
        }


        return userInfo;
    }

    private Map<String,List<MenuInfo>> convertMenuInfos(Map<String, List<MenuInfoResponseDTO>> menusResponse){
        if(CollectionUtils.isEmpty(menusResponse)){
            return null;
        }
        Map<String,List<MenuInfo>> menusMap = new HashMap<>();
        for (Map.Entry<String,List<MenuInfoResponseDTO>> entry : menusResponse.entrySet()){
            menusMap.put(entry.getKey(),convertMenuInfos(entry.getValue()));
        }
        return menusMap;
    }

    private List<MenuInfo> convertMenuInfos(List<MenuInfoResponseDTO> menuInfoResponseDTOS){
        if(org.apache.commons.collections.CollectionUtils.isEmpty(menuInfoResponseDTOS)){
            return null;
        }

        List<MenuInfo> menuInfos = new ArrayList<>();
        for (MenuInfoResponseDTO menuInfoResponseDTO : menuInfoResponseDTOS){
            MenuInfo menuInfo = new MenuInfo();
            menuInfo.setIcon(menuInfoResponseDTO.getIcon());
            menuInfo.setId(menuInfoResponseDTO.getId());
            menuInfo.setName(menuInfoResponseDTO.getName());
            menuInfo.setOrders(menuInfoResponseDTO.getOrders());
            menuInfo.setPid(menuInfoResponseDTO.getPid());
            menuInfo.setSystemId(menuInfoResponseDTO.getSystemId());
            menuInfo.setTarget(menuInfoResponseDTO.getTarget());
            menuInfo.setUrl(menuInfoResponseDTO.getUrl());
            menuInfo.setVersion(menuInfoResponseDTO.getVersion());
            menuInfo.setChildMenus(convertMenuInfos(menuInfoResponseDTO.getChildMenus()));
            menuInfos.add(menuInfo);
        }
        return menuInfos;
    }



    public UserApi getUserApi() {
        return userApi;
    }

    public void setUserApi(UserApi userApi) {
        this.userApi = userApi;
    }
}
