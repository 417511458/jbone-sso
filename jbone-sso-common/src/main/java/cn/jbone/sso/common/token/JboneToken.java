package cn.jbone.sso.common.token;

import cn.jbone.sso.common.domain.UserInfo;

import java.io.Serializable;
import java.util.Map;

/**
 * Jbone票据
 */
public class JboneToken implements Serializable {
    public static final String PREFIX = "jbone:";
    private static final long serialVersionUID = -8975152371274081154L;
    private long creationTime;
    private String id;
    private long timeout;
    private UserInfo userInfo;
    private Map<String,Object> attributes;

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
