package cn.jbone.sso.support.authentication.custom;

import cn.jbone.sso.common.SsoConstants;
import cn.jbone.sso.common.domain.UserInfo;
import cn.jbone.sso.common.token.JboneToken;
import com.alibaba.fastjson.JSON;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
        UserInfo userInfo = JSON.parseObject(((List) attributes.get(SsoConstants.USER_INFO)).get(0).toString(),UserInfo.class);
        jboneToken.setUserInfo(userInfo);
        this.redisTemplate.opsForValue().set(JboneToken.PREFIX + accessToken.getId(),jboneToken,accessToken.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS);
        return accessToken;
    }


}
