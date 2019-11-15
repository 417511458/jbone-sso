package cn.jbone.sso.server.authentication;

import cn.jbone.sso.common.SsoConstants;
import cn.jbone.sso.common.domain.*;
import cn.jbone.sso.common.token.JboneToken;
import cn.jbone.system.common.UserResponseDO;
import cn.jbone.system.common.dto.response.MenuInfoResponseDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JboneAccessTokenFactory extends DefaultAccessTokenFactory {

    RedisTemplate<String, JboneToken> redisTemplate;

    public JboneAccessTokenFactory(ExpirationPolicy expirationPolicy) {
        super(expirationPolicy);
    }

    public JboneAccessTokenFactory(UniqueTicketIdGenerator accessTokenIdGenerator, ExpirationPolicy expirationPolicy,RedisTemplate<String, JboneToken> redisTemplate) {
        super(accessTokenIdGenerator, expirationPolicy);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AccessToken create(Service service, Authentication authentication, TicketGrantingTicket ticketGrantingTicket, Collection<String> scopes) {
        AccessToken accessToken = super.create(service, authentication, ticketGrantingTicket, scopes);
        JboneToken jboneToken = new JboneToken();
        jboneToken.setId(accessToken.getId());
        jboneToken.setCreationTime(new Date().getTime());
        jboneToken.setTimeout(accessToken.getExpirationPolicy().getTimeToLive());

        Map<String,Object> attributes = ticketGrantingTicket.getAuthentication().getPrincipal().getAttributes();
        UserResponseDO userResponseDO = (UserResponseDO) ((List) attributes.get(SsoConstants.USER_INFO)).get(0);
        jboneToken.setUserInfo(converUserInfo(userResponseDO));
        this.redisTemplate.opsForValue().set(JboneToken.PREFIX + accessToken.getId(),jboneToken,accessToken.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
        return accessToken;
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
            authInfo.setMenus(convertMenuInfos(userResponseDO.getAuthInfo().getMenus()));

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

    private List<MenuInfo> convertMenuInfos(List<MenuInfoResponseDTO> menuInfoResponseDTOS){
        if(CollectionUtils.isEmpty(menuInfoResponseDTOS)){
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
}
