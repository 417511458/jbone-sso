package cn.jbone.sso.common.domain;

import java.io.Serializable;

/**
 * 用户信息
 */
public class UserInfo implements Serializable {


    private static final long serialVersionUID = -406746474412278810L;
    private UserBaseInfo baseInfo;
    private UserAuthInfo authInfo;
    private UserSecurityInfo securityInfo;

    public UserBaseInfo getBaseInfo() {
        return baseInfo;
    }

    public void setBaseInfo(UserBaseInfo baseInfo) {
        this.baseInfo = baseInfo;
    }

    public UserAuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(UserAuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public UserSecurityInfo getSecurityInfo() {
        return securityInfo;
    }

    public void setSecurityInfo(UserSecurityInfo securityInfo) {
        this.securityInfo = securityInfo;
    }

    @Override
    public String toString() {
        return this.baseInfo.getRealname();
    }
}
