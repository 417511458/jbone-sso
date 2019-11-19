package cn.jbone.sso.common.domain;

import java.io.Serializable;

/**
 * 用户安全信息
 */
public class UserSecurityInfo implements Serializable {

    private static final long serialVersionUID = -6920169168405583768L;
    private String password;
    private int locked;
    private String salt;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLocked() {
        return locked;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
