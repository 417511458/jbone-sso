package cn.jbone.sso.client.session;

import cn.jbone.sso.common.domain.UserInfo;
import com.alibaba.fastjson.JSON;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.SimplePrincipalCollection;

import java.util.Map;

/**
 * 自定义Cas Session
 */
public class JboneCasSession extends SimpleSession {
    private String serverName;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public UserInfo getUserInfo(){
        UserInfo userInfo = null;
        for (Map.Entry<Object,Object> entry : getAttributes().entrySet()){
            if(entry.getValue() instanceof SimplePrincipalCollection){
                SimplePrincipalCollection principalCollection = (SimplePrincipalCollection)entry.getValue();
                userInfo = principalCollection.oneByType(UserInfo.class);
                return userInfo;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
