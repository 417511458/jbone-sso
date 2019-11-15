package cn.jbone.sso.common.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户权限信息
 */
public class UserAuthInfo implements Serializable {
    private Set<String> permissions;
    private Set<String> roles;
    private List<MenuInfo> menus;

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public List<MenuInfo> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuInfo> menus) {
        this.menus = menus;
    }
}
